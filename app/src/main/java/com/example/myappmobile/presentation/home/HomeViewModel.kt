package com.example.myappmobile.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.access.RoleAccessManager
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.toApiException
import com.example.myappmobile.domain.BannerData
import com.example.myappmobile.domain.Category
import com.example.myappmobile.domain.Product
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val productRepository = AppContainer.productRepository
    private companion object {
        const val TAG = "HomeViewModel"
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeHomeState()
        loadHomeData()
    }

    private fun observeHomeState() {
        viewModelScope.launch {
            combine(
                productRepository.observeAllProducts(),
                AppContainer.authRepository.currentUser,
                productRepository.favoriteMessage,
                productRepository.favoriteOperationProductIds,
            ) { allProducts, user, favoriteMessage, pendingFavoriteIds ->
                val access = RoleAccessManager.capabilities(user)
                val featuredProducts = allProducts.take(4)
                val newArrivals = allProducts.takeLast(4).reversed().ifEmpty { allProducts.take(4) }
                HomeUiState(
                    isLoading = _uiState.value.isLoading && allProducts.isEmpty(),
                    banner = deriveBanner(
                        featuredProducts = featuredProducts,
                        newArrivals = newArrivals,
                        allProducts = allProducts,
                    ),
                    categories = deriveCategories(allProducts),
                    featuredProducts = featuredProducts,
                    newArrivals = newArrivals,
                    emailInput = _uiState.value.emailInput,
                    isSubscribed = _uiState.value.isSubscribed,
                    canUseWishlist = access.canUseWishlist,
                    error = _uiState.value.error,
                    favoriteMessage = favoriteMessage,
                    pendingFavoriteIds = pendingFavoriteIds,
                )
            }.collectLatest { derivedState ->
                _uiState.value = derivedState
            }
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val allProducts = productRepository.getAllProducts()
                Log.d(
                    TAG,
                    "Home loaded successfully. all=${allProducts.size}",
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                    )
                }
            }.onFailure { throwable ->
                val apiError = throwable.toApiException()
                Log.d(TAG, "Home API load failed: ${apiError.message}")
                val access = RoleAccessManager.capabilities(AppContainer.authRepository.currentUser.value)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        canUseWishlist = access.canUseWishlist,
                        error = apiError.message,
                    )
                }
            }
        }
    }

    fun retry() {
        loadHomeData()
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(emailInput = value) }
    }

    fun onSubscribe() {
        _uiState.update { it.copy(isSubscribed = true) }
    }

    fun onToggleFavorite(productId: String) {
        if (!_uiState.value.canUseWishlist || productId in _uiState.value.pendingFavoriteIds) return
        viewModelScope.launch {
            productRepository.toggleFavorite(productId)
        }
    }

    fun clearFavoriteMessage() {
        productRepository.clearFavoriteMessage()
    }

    private fun deriveCategories(products: List<Product>): List<Category> = products
        .map(Product::category)
        .filter { category -> category.name.isNotBlank() }
        .distinctBy(Category::id)
        .sortedBy(Category::name)

    private fun deriveBanner(
        featuredProducts: List<Product>,
        newArrivals: List<Product>,
        allProducts: List<Product>,
    ): BannerData? {
        val leadProduct = featuredProducts.firstOrNull()
            ?: newArrivals.firstOrNull()
            ?: allProducts.firstOrNull()
            ?: return null
        return BannerData(
            title = leadProduct.name,
            subtitle = "Discover handcrafted ${leadProduct.category.name.lowercase()} from ${leadProduct.studio} on FLORA.",
            ctaText = "EXPLORE COLLECTION",
            imageUrl = leadProduct.imageUrl,
        )
    }
}
