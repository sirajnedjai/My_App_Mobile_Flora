package com.example.myappmobile.presentation.checkout

import com.example.myappmobile.domain.model.CartItem

data class CheckoutUiState(
    val fullName: String = "",
    val street: String = "",
    val city: String = "",
    val postalCode: String = "",
    val country: String = "",
    val shippingMethod: String = "Standard Delivery",
    val paymentMethod: String = "Card",
    val cardNumber: String = "",
    val cardName: String = "",
    val expiryDate: String = "",
    val cvv: String = "",
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val shippingCost: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val isPlacingOrder: Boolean = false,
    val errorMessage: String? = null,
) {
    val itemCount: Int
        get() = items.sumOf { it.quantity }

    val isAddressValid: Boolean
        get() = fullName.isNotBlank() &&
            street.isNotBlank() &&
            city.isNotBlank() &&
            postalCode.isNotBlank() &&
            country.isNotBlank()

    val canPlaceOrder: Boolean
        get() = items.isNotEmpty() && isAddressValid && !isPlacingOrder
}

data class ShippingOptionUi(
    val title: String,
    val subtitle: String,
    val priceLabel: String,
    val priceValue: Double,
)

data class PaymentOptionUi(
    val id: String,
    val title: String,
)
