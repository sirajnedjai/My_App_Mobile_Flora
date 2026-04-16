package com.example.myappmobile.presentation.productdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.MockData
import com.example.myappmobile.domain.Review
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductDetailsViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val productId: String = savedStateHandle["productId"] ?: MockData.sculptedRippleVase.id
    private val getProductDetailsUseCase = AppContainer.getProductDetailsUseCase
    private val addToCartUseCase = AppContainer.addToCartUseCase
    private val productReviewRepository = AppContainer.productReviewRepository

    private val _uiState = MutableStateFlow(ProductDetailsUiState())
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(400)
            val productDetails = getProductDetailsUseCase(productId)
            val mergedReviews = mergeReviews(productDetails.reviews, productReviewRepository.getReviews(productDetails.id))
            val sellerId = productDetails.sellerId.ifBlank {
                MockData.storeIdForProduct(
                    productId = productDetails.id,
                    studioName = productDetails.artist.name,
                )
            }
            val eligibility = productReviewRepository.getReviewEligibility(
                productId = productDetails.id,
                sellerId = sellerId,
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    product = productDetails.copy(reviews = mergedReviews),
                    sellerId = sellerId,
                    selectedImageIndex = 0,
                    addedToCart = false,
                    reserveRequested = false,
                    canWriteReviews = eligibility.canReview,
                    restrictionMessage = eligibility.message,
                )
            }
        }
    }

    fun onSelectImage(index: Int) {
        _uiState.update { it.copy(selectedImageIndex = index) }
    }

    fun onAddToCart() {
        viewModelScope.launch {
            _uiState.value.product?.let { productDetails ->
                val product = AppContainer.productRepository.getAllProducts()
                    .firstOrNull { it.id == productDetails.id }
                    ?: MockData.findProductById(productDetails.id)

                if (product != null) {
                    addToCartUseCase(product)
                    _uiState.update { it.copy(addedToCart = true) }
                }
            }
        }
    }

    fun onReservePickup() {
        _uiState.update { it.copy(reserveRequested = true) }
    }

    fun onRatingSelected(rating: Int) {
        if (!_uiState.value.canWriteReviews) return
        _uiState.update { it.copy(selectedRating = rating, reviewError = null, reviewSuccess = null) }
    }

    fun onReviewInputChanged(value: String) {
        if (!_uiState.value.canWriteReviews) return
        _uiState.update { it.copy(reviewInput = value, reviewError = null, reviewSuccess = null) }
    }

    fun submitReview() {
        val snapshot = _uiState.value
        if (!snapshot.canWriteReviews) {
            _uiState.update {
                it.copy(
                    reviewError = snapshot.restrictionMessage
                        ?: "Please order this product first and wait until it is marked as delivered before leaving a review.",
                )
            }
            return
        }
        val product = snapshot.product ?: return
        val result = productReviewRepository.submitReview(
            productId = product.id,
            sellerId = snapshot.sellerId.orEmpty(),
            rating = snapshot.selectedRating,
            text = snapshot.reviewInput,
        )

        result.fold(
            onSuccess = {
                val mergedReviews = mergeReviews(product.reviews, productReviewRepository.getReviews(product.id))
                _uiState.update { state ->
                    state.copy(
                        product = product.copy(reviews = mergedReviews),
                        selectedRating = 0,
                        reviewInput = "",
                        reviewError = null,
                        reviewSuccess = "Your rating and review have been posted.",
                    )
                }
            },
            onFailure = { error ->
                _uiState.update { state ->
                    state.copy(
                        reviewError = error.message ?: "We couldn't submit your review right now.",
                        reviewSuccess = null,
                    )
                }
            },
        )
    }

    private fun mergeReviews(base: List<Review>, local: List<Review>): List<Review> =
        (local + base).distinctBy(Review::id)
}
