package com.example.myappmobile.presentation.cart

import com.example.myappmobile.domain.Product

data class CartUiState(
    val items: List<Product> = emptyList(),
) {
    val itemsCount: Int get() = items.size
    val subtotal: Double get() = items.sumOf(Product::price)
}
