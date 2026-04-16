package com.example.myappmobile.presentation.seller.storefront

import com.example.myappmobile.domain.model.Product
import com.example.myappmobile.domain.model.Review
import com.example.myappmobile.domain.model.Store

data class StoreFrontUiState(
    val store: Store? = null,
    val products: List<Product> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val isAlternateProductLayout: Boolean = false,
)
