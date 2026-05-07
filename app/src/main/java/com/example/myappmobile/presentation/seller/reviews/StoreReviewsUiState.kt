package com.example.myappmobile.presentation.seller.reviews

import com.example.myappmobile.domain.model.Review

data class StoreReviewsUiState(
    val reviews: List<Review> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
