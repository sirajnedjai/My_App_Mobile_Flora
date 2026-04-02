package com.example.myappmobile.presentation.wishlist

import com.example.myappmobile.domain.Product

data class WishlistUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true,
) {
    val isEmpty: Boolean
        get() = !isLoading && products.isEmpty()
}
