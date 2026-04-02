package com.example.myappmobile.domain.model

data class CartItem(
    val id: String,
    val product: Product,
    val quantity: Int = 1,
    val selectedVariant: String = "",
    val selectedSize: String = "",
)