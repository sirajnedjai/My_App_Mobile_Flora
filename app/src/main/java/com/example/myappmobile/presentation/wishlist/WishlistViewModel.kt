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

    val uiState: StateFlow<WishlistUiState> = combine(
        AppContainer.authRepository.currentUser,
        searchQuery,
        productRepository.observeAllProducts(),
    ) { user, query, _ ->
        val access = RoleAccessManager.capabilities(user)
        if (!access.canUseWishlist) {
            WishlistUiState(
                query = query,
                isBuyer = false,
                restrictionMessage = access.buyersOnlyMessage,
                isLoading = false,
            )
        } else {
            val products = productRepository.getFavoriteProducts().filter { product ->
                query.isBlank() ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.studio.contains(query, ignoreCase = true) ||
                    product.category.name.contains(query, ignoreCase = true)
            }
            WishlistUiState(
                products = products,
                query = query,
                isBuyer = true,
                isLoading = false,
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
        searchQuery.update { it }
    }

    fun onSearchQueryChanged(value: String) {
        searchQuery.value = value
    }

    fun onRemoveFromWishlist(productId: String) {
        if (!uiState.value.isBuyer) return
        viewModelScope.launch {
            productRepository.toggleFavorite(productId)
            refresh()
        }
    }

    fun onAddToCart(productId: String) {
        viewModelScope.launch {
            productRepository.getAllProducts()
                .firstOrNull { it.id == productId }
                ?.let { product -> cartRepository.addToCart(product) }
        }
    }
}
