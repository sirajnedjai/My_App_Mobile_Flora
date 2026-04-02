package com.example.myappmobile.presentation.productdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.MockData
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

    private val _uiState = MutableStateFlow(ProductDetailsUiState())
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(400)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    product = getProductDetailsUseCase(productId),
                    selectedImageIndex = 0,
                    addedToCart = false,
                    reserveRequested = false,
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
}
