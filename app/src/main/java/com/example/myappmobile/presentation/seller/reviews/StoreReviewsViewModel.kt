package com.example.myappmobile.presentation.seller.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoreReviewsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(StoreReviewsUiState())
    val uiState: StateFlow<StoreReviewsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = StoreReviewsUiState(
                reviews = AppContainer.storeRepository.getStoreReviews("s1")
            )
        }
    }
}
