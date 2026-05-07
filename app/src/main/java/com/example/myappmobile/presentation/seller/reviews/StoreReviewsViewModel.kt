package com.example.myappmobile.presentation.seller.reviews

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.toApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoreReviewsViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val sellerId: String = savedStateHandle.get<String>("sellerId").orEmpty()

    private val _uiState = MutableStateFlow(StoreReviewsUiState())
    val uiState: StateFlow<StoreReviewsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (sellerId.isBlank()) {
                _uiState.value = StoreReviewsUiState(
                    isLoading = false,
                    errorMessage = "This store could not be opened.",
                )
                return@launch
            }
            runCatching {
                AppContainer.storeRepository.getStoreReviews(sellerId)
            }.fold(
                onSuccess = { reviews ->
                    _uiState.value = StoreReviewsUiState(
                        reviews = reviews,
                        isLoading = false,
                    )
                },
                onFailure = { error ->
                    _uiState.value = StoreReviewsUiState(
                        isLoading = false,
                        errorMessage = error.toApiException().message,
                    )
                },
            )
        }
    }
}
