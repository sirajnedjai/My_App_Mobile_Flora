package com.example.myappmobile.presentation.seller.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AboutStoreViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AboutStoreUiState())
    val uiState: StateFlow<AboutStoreUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = AboutStoreUiState(
                store = AppContainer.getStoreDetailsUseCase("s1")
            )
        }
    }
}
