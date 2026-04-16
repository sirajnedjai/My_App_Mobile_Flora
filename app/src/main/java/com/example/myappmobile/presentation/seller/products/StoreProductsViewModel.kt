package com.example.myappmobile.presentation.seller.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoreProductsViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val sellerId: String = savedStateHandle["sellerId"] ?: "s1"

    private val _uiState = MutableStateFlow(StoreProductsUiState())
    val uiState: StateFlow<StoreProductsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = StoreProductsUiState(
                products = AppContainer.getStoreProductsUseCase(sellerId)
            )
        }
    }
}
