package com.example.myappmobile.presentation.seller.products

data class StoreProductsUiState(
    val products: List<com.example.myappmobile.domain.model.Product> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
