package com.example.myappmobile.presentation.checkout.payment

data class PaymentUiState(
    val paymentMethod: String = "card",
    val subtotal: Double = 0.0,
    val itemCount: Int = 0,
)
