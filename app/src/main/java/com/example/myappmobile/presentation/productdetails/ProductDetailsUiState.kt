package com.example.myappmobile.presentation.productdetails

import com.example.myappmobile.domain.ProductDetails

data class ProductDetailsUiState(
    val isLoading: Boolean = true,
    val product: ProductDetails? = null,
    val selectedImageIndex: Int = 0,
    val addedToCart: Boolean = false,
    val reserveRequested: Boolean = false,
    val error: String? = null,
)