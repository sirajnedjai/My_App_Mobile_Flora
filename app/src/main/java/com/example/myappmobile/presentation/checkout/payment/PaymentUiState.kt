package com.example.myappmobile.presentation.checkout.payment

data class PaymentUiState(
    val paymentMethod: String = "Card ending in 4242",
    val subtotal: Double = 0.0,
    val itemCount: Int = 0,
)
