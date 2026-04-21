package com.example.myappmobile.presentation.seller.manageproducts

import com.example.myappmobile.domain.model.SellerManagedProductDetails

data class SellerProductDetailUiState(
    val isLoading: Boolean = true,
    val isSeller: Boolean = false,
    val details: SellerManagedProductDetails? = null,
    val errorMessage: String? = null,
    val pendingDelete: Boolean = false,
    val isDeleting: Boolean = false,
    val deleted: Boolean = false,
)
