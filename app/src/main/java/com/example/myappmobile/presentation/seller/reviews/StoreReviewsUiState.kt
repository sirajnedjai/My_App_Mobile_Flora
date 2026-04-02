package com.example.myappmobile.presentation.seller.reviews

import com.example.myappmobile.domain.model.Review

data class StoreReviewsUiState(
    val reviews: List<Review> = emptyList(),
)
