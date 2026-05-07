package com.example.myappmobile.presentation.seller.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.toApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoreProductsViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val sellerId: String = savedStateHandle.get<String>("sellerId").orEmpty()

    private val _uiState = MutableStateFlow(StoreProductsUiState())
    val uiState: StateFlow<StoreProductsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (sellerId.isBlank()) {
                _uiState.value = StoreProductsUiState(
                    isLoading = false,
                    errorMessage = "This store could not be opened.",
                )
                return@launch
            }
            runCatching {
                AppContainer.getStoreProductsUseCase(sellerId)
            }.fold(
                onSuccess = { products ->
                    _uiState.value = StoreProductsUiState(
                        products = products,
                        isLoading = false,
                    )
                },
                onFailure = { error ->
                    _uiState.value = StoreProductsUiState(
                        isLoading = false,
                        errorMessage = error.toApiException().message,
                    )
                },
            )
        }
    }
}
