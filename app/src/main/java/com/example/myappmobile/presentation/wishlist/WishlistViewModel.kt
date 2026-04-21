package com.example.myappmobile.presentation.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.access.RoleAccessManager
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WishlistViewModel : ViewModel() {

    private val productRepository = AppContainer.productRepository
    private val cartRepository = AppContainer.cartRepository

    private val searchQuery = MutableStateFlow("")
    private val wishlistStatus = combine(
        productRepository.favoriteMessage,
        productRepository.favoriteOperationProductIds,
        productRepository.isRefreshingFavorites,
    ) { statusMessage, pendingProductIds, isRefreshing ->
        WishlistStatus(
            statusMessage = statusMessage,
            pendingProductIds = pendingProductIds,
            isRefreshing = isRefreshing,
        )
    }

    val uiState: StateFlow<WishlistUiState> = combine(
        AppContainer.authRepository.currentUser,
        searchQuery,
        productRepository.observeFavoriteProducts(),
        wishlistStatus,
    ) { user, query, favorites, wishlistStatus ->
        val access = RoleAccessManager.capabilities(user)
        if (!access.canUseWishlist) {
            WishlistUiState(
                query = query,
                isBuyer = false,
                restrictionMessage = access.buyersOnlyMessage,
                isLoading = false,
                statusMessage = wishlistStatus.statusMessage,
            )
        } else {
            val products = favorites.filter { product ->
                query.isBlank() ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.studio.contains(query, ignoreCase = true) ||
                    product.category.name.contains(query, ignoreCase = true)
            }
            WishlistUiState(
                products = products,
                query = query,
                isBuyer = true,
                isLoading = wishlistStatus.isRefreshing && favorites.isEmpty(),
                statusMessage = wishlistStatus.statusMessage,
                pendingProductIds = wishlistStatus.pendingProductIds,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WishlistUiState(),
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            productRepository.refreshFavorites()
        }
    }

    fun onSearchQueryChanged(value: String) {
        searchQuery.value = value
    }

    fun onRemoveFromWishlist(productId: String) {
        if (!uiState.value.isBuyer || productId in uiState.value.pendingProductIds) return
        viewModelScope.launch {
            productRepository.toggleFavorite(productId)
        }
    }

    fun clearStatusMessage() {
        productRepository.clearFavoriteMessage()
    }

    private data class WishlistStatus(
        val statusMessage: String?,
        val pendingProductIds: Set<String>,
        val isRefreshing: Boolean,
    )

    fun onAddToCart(productId: String) {
        viewModelScope.launch {
            productRepository.getAllProducts()
                .firstOrNull { it.id == productId }
                ?.let { product -> cartRepository.addToCart(product) }
        }
    }
}
