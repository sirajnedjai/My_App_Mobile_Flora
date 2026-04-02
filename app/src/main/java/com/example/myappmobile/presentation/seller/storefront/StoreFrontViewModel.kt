package com.example.myappmobile.presentation.seller.storefront

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoreFrontViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(StoreFrontUiState())
    val uiState: StateFlow<StoreFrontUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = StoreFrontUiState(
                store = AppContainer.getStoreDetailsUseCase("s1")
            )
        }
    }
}
