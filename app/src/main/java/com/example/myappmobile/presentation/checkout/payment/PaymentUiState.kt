package com.example.myappmobile.presentation.checkout.payment

data class PaymentUiState(
    val paymentMethod: String = "card",
    val subtotal: Double = 0.0,
    val itemCount: Int = 0,
    val shippingMethod: String = "home_delivery",
    val shippingCost: Double = 300.0,
    val total: Double = 0.0,
    val isPlacingOrder: Boolean = false,
    val errorMessage: String? = null,
)
