package com.example.myappmobile.presentation.seller.about

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.toApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AboutStoreViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val sellerId: String = savedStateHandle.get<String>("sellerId").orEmpty()

    private val _uiState = MutableStateFlow(AboutStoreUiState())
    val uiState: StateFlow<AboutStoreUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (sellerId.isBlank()) {
                _uiState.value = AboutStoreUiState(
                    isLoading = false,
                    errorMessage = "This store could not be opened.",
                )
                return@launch
            }
            runCatching {
                AppContainer.getStoreDetailsUseCase(sellerId)
            }.fold(
                onSuccess = { store ->
                    _uiState.value = AboutStoreUiState(
                        store = store,
                        isLoading = false,
                    )
                },
                onFailure = { error ->
                    _uiState.value = AboutStoreUiState(
                        isLoading = false,
                        errorMessage = error.toApiException().message,
                    )
                },
            )
        }
    }
}
