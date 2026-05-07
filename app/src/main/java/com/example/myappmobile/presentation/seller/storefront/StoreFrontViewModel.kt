package com.example.myappmobile.presentation.seller.storefront

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.toApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StoreFrontViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val sellerId: String = savedStateHandle.get<String>("sellerId").orEmpty()

    private val _uiState = MutableStateFlow(StoreFrontUiState())
    val uiState: StateFlow<StoreFrontUiState> = _uiState

    init {
        loadStorefront()
    }

    fun onToggleProductLayout() {
        _uiState.update { state ->
            state.copy(isAlternateProductLayout = !state.isAlternateProductLayout)
        }
    }

    private fun loadStorefront() {
        viewModelScope.launch {
            if (sellerId.isBlank()) {
                _uiState.value = StoreFrontUiState(
                    isLoading = false,
                    errorMessage = "This store could not be opened.",
                )
                return@launch
            }

            runCatching {
                StoreFrontUiState(
                    store = AppContainer.getStoreDetailsUseCase(sellerId),
                    products = AppContainer.getStoreProductsUseCase(sellerId),
                    reviews = AppContainer.storeRepository.getStoreReviews(sellerId),
                    isLoading = false,
                )
            }.fold(
                onSuccess = { state -> _uiState.value = state },
                onFailure = { error ->
                    _uiState.value = StoreFrontUiState(
                        isLoading = false,
                        errorMessage = error.toApiException().message,
                    )
                },
            )
        }
    }
}
