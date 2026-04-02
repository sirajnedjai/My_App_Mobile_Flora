package com.example.myappmobile.presentation.checkout.confirmation

import androidx.lifecycle.ViewModel
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConfirmationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        ConfirmationUiState(order = AppContainer.orderRepository.getLatestOrder())
    )
    val uiState: StateFlow<ConfirmationUiState> = _uiState.asStateFlow()
}
