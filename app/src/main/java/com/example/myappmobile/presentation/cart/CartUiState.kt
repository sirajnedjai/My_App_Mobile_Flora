package com.example.myappmobile.presentation.cart

import com.example.myappmobile.domain.model.CartItem

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val promoCode: String = "",
    val promoApplied: Boolean = false,
    val promoMessage: String? = null,
    val statusMessage: String? = null,
) {
    val itemsCount: Int get() = items.sumOf(CartItem::quantity)
    val uniqueItemsCount: Int get() = items.size
    val subtotal: Double get() = items.sumOf { it.product.price * it.quantity }
    val shippingFee: Double get() = if (items.isEmpty()) 0.0 else 12.0
    val discount: Double get() = if (promoApplied) subtotal * 0.10 else 0.0
    val total: Double get() = subtotal + shippingFee - discount
}
