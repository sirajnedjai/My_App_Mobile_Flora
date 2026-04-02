package com.example.myappmobile.presentation.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WishlistViewModel : ViewModel() {

    private val productRepository = AppContainer.productRepository
    private val cartRepository = AppContainer.cartRepository

    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            _uiState.value = WishlistUiState(
                products = productRepository.getFavoriteProducts(),
                isLoading = false,
            )
        }
    }

    fun onRemoveFromWishlist(productId: String) {
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
