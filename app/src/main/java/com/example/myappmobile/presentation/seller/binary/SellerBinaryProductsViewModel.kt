package com.example.myappmobile.presentation.seller.binary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.toApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SellerBinaryProductsViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val sellerId: String = savedStateHandle.get<String>("sellerId").orEmpty()

    private val _uiState = MutableStateFlow(SellerBinaryProductsUiState())
    val uiState: StateFlow<SellerBinaryProductsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (sellerId.isBlank()) {
                _uiState.value = SellerBinaryProductsUiState(
                    errorMessage = "This store could not be opened.",
                )
                return@launch
            }

            runCatching {
                val store = AppContainer.storeRepository.getStoreDetails(sellerId)
                val products = AppContainer.storeRepository.getStoreProducts(sellerId)
                SellerBinaryProductsUiState(
                    storeName = store.name,
                    products = products,
                    isLoading = false,
                )
            }.fold(
                onSuccess = { state -> _uiState.value = state },
                onFailure = { error ->
                    _uiState.value = SellerBinaryProductsUiState(
                        isLoading = false,
                        errorMessage = error.toApiException().message,
                    )
                },
            )
        }
    }
}
