package com.example.myappmobile.presentation.productdetails

import com.example.myappmobile.domain.ProductDetails

data class ProductDetailsUiState(
    val isLoading: Boolean = true,
    val product: ProductDetails? = null,
    val sellerId: String? = null,
    val reviewOrderId: String? = null,
    val selectedImageIndex: Int = 0,
    val addedToCart: Boolean = false,
    val reserveRequested: Boolean = false,
    val canWriteReviews: Boolean = true,
    val restrictionMessage: String? = null,
    val selectedRating: Int = 0,
    val reviewInput: String = "",
    val reviewError: String? = null,
    val reviewSuccess: String? = null,
    val isSubmittingReview: Boolean = false,
    val error: String? = null,
    val favoriteMessage: String? = null,
    val isFavoriteUpdating: Boolean = false,
)
