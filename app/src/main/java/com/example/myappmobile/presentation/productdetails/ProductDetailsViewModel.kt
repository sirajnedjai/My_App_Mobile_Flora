package com.example.myappmobile.presentation.productdetails

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.toApiException
import com.example.myappmobile.domain.Category
import com.example.myappmobile.domain.Product
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductDetailsViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private companion object {
        const val TAG = "ProductDetailsVM"
    }

    private val productId: String = savedStateHandle.get<String>("productId").orEmpty()
    private val requestedOrderId: String = savedStateHandle.get<String>("orderId").orEmpty()
    private val getProductDetailsUseCase = AppContainer.getProductDetailsUseCase
    private val addToCartUseCase = AppContainer.addToCartUseCase
    private val productReviewRepository = AppContainer.productReviewRepository
    private val productRepository = AppContainer.productRepository

    private val _uiState = MutableStateFlow(ProductDetailsUiState())
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    init {
        observeProductUpdates()
        loadProduct()
    }

    private fun observeProductUpdates() {
        viewModelScope.launch {
            productRepository.observeAllProducts().collectLatest { products ->
                val productsById = products.associateBy(Product::id)
                _uiState.update { state ->
                    val current = state.product ?: return@update state.copy(
                        favoriteMessage = productRepository.favoriteMessage.value,
                        isFavoriteUpdating = productId in productRepository.favoriteOperationProductIds.value,
                    )
                    val updatedCurrent = productsById[current.id]
                    state.copy(
                        product = current.copy(
                            isFavorited = updatedCurrent?.isFavorited ?: current.isFavorited,
                            similarProducts = current.similarProducts.map { similar ->
                                val synced = productsById[similar.id]
                                if (synced == null) similar else similar.copy(isFavorited = synced.isFavorited)
                            },
                        ),
                        favoriteMessage = productRepository.favoriteMessage.value,
                        isFavoriteUpdating = current.id in productRepository.favoriteOperationProductIds.value,
                    )
                }
            }
        }
        viewModelScope.launch {
            productRepository.favoriteMessage.collectLatest { message ->
                _uiState.update { state ->
                    state.copy(
                        favoriteMessage = message,
                        isFavoriteUpdating = productId in productRepository.favoriteOperationProductIds.value,
                    )
                }
            }
        }
        viewModelScope.launch {
            productRepository.favoriteOperationProductIds.collectLatest { pendingIds ->
                _uiState.update { state ->
                    state.copy(isFavoriteUpdating = productId in pendingIds)
                }
            }
        }
    }

    fun retry() {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            if (productId.isBlank()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "This product could not be opened.",
                    )
                }
                return@launch
            }

            Log.d(TAG, "Loading product details. productId=$productId")
            _uiState.update { it.copy(isLoading = true, error = null, reviewError = null, reviewSuccess = null) }
            runCatching {
                val productDetails = getProductDetailsUseCase(productId)
                val sellerId = productDetails.sellerId.ifBlank { productDetails.artist.id }
                val eligibility = productReviewRepository.getReviewEligibility(
                    productId = productDetails.id,
                    sellerId = sellerId,
                )
                val resolvedReviewOrderId = requestedOrderId.ifBlank { eligibility.orderId.orEmpty() }
                Log.d(
                    TAG,
                    "Product details loaded. id=${productDetails.id}, images=${productDetails.images.size}, reviews=${productDetails.reviews.size}, sellerId=$sellerId reviewOrderId=$resolvedReviewOrderId",
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        product = productDetails,
                        sellerId = sellerId,
                        reviewOrderId = resolvedReviewOrderId.ifBlank { null },
                        selectedImageIndex = 0,
                        addedToCart = false,
                        reserveRequested = false,
                        canWriteReviews = eligibility.canReview && resolvedReviewOrderId.isNotBlank(),
                        restrictionMessage = if (resolvedReviewOrderId.isBlank()) {
                            eligibility.message ?: "You can only review products you purchased and received."
                        } else {
                            eligibility.message
                        },
                        isSubmittingReview = false,
                        error = null,
                    )
                }
            }.onFailure { throwable ->
                val message = throwable.toApiException().message
                Log.d(TAG, "Product details failed. productId=$productId error=$message")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSubmittingReview = false,
                        product = null,
                        error = message,
                    )
                }
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
                    ?: productDetails.toProduct()

                addToCartUseCase(product)
                _uiState.update { it.copy(addedToCart = true) }
            }
        }
    }

    fun onReservePickup() {
        _uiState.update { it.copy(reserveRequested = true) }
    }

    fun onToggleFavorite() {
        val product = _uiState.value.product ?: return
        if (_uiState.value.isFavoriteUpdating) return
        viewModelScope.launch {
            productRepository.toggleFavorite(product.id)
        }
    }

    fun clearFavoriteMessage() {
        productRepository.clearFavoriteMessage()
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
        if (snapshot.isSubmittingReview) return
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
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReview = true, reviewError = null, reviewSuccess = null) }
            val result = productReviewRepository.submitReview(
                productId = product.id,
                orderId = snapshot.reviewOrderId.orEmpty(),
                sellerId = snapshot.sellerId.orEmpty(),
                rating = snapshot.selectedRating,
                text = snapshot.reviewInput,
            )

            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            selectedRating = 0,
                            reviewInput = "",
                            reviewError = null,
                            reviewSuccess = "Your rating and review have been posted.",
                        )
                    }
                    loadProduct()
                },
                onFailure = { error ->
                    _uiState.update { state ->
                        state.copy(
                            isSubmittingReview = false,
                            reviewError = error.message ?: "We couldn't submit your review right now.",
                            reviewSuccess = null,
                        )
                    }
                },
            )
        }
    }

    private fun com.example.myappmobile.domain.ProductDetails.toProduct(): Product = Product(
        id = id,
        name = name,
        price = price,
        imageUrl = images.firstOrNull().orEmpty(),
        studio = artist.studioName.ifBlank { artist.name },
        category = Category(
            id = collectionLabel.lowercase(),
            name = collectionLabel.ifBlank { "Curated" },
            iconRes = android.R.drawable.ic_menu_gallery,
        ),
        isFavorited = isFavorited,
    )
}
