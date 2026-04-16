package com.example.myappmobile.presentation.seller.binary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SellerBinaryProductsViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val sellerId: String = savedStateHandle["sellerId"] ?: "s1"

    private val _uiState = MutableStateFlow(SellerBinaryProductsUiState())
    val uiState: StateFlow<SellerBinaryProductsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val store = AppContainer.storeRepository.getStoreDetails(sellerId)
            val products = AppContainer.storeRepository.getStoreProducts(sellerId)
            _uiState.value = SellerBinaryProductsUiState(
                storeName = store.name,
                products = products,
            )
        }
    }
}
