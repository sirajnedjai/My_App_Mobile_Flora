package com.example.myappmobile.presentation.wishlist

import com.example.myappmobile.domain.Product

data class WishlistUiState(
    val products: List<Product> = emptyList(),
    val query: String = "",
    val isBuyer: Boolean = true,
    val restrictionMessage: String? = null,
    val isLoading: Boolean = true,
) {
    val isEmpty: Boolean
        get() = !isLoading && products.isEmpty()
}
