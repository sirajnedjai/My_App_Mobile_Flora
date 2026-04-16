package com.example.myappmobile.presentation.seller.binary

import com.example.myappmobile.domain.model.Product

data class SellerBinaryProductsUiState(
    val storeName: String = "",
    val products: List<Product> = emptyList(),
)
