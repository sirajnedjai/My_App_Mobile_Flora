package com.example.myappmobile.presentation.checkout.shipping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShippingViewModel : ViewModel() {

    private val repository = AppContainer.orderRepository
    private val _uiState = MutableStateFlow(
        ShippingUiState(shippingMethod = repository.checkoutDraft.value.shippingMethod)
    )
    val uiState: StateFlow<ShippingUiState> = _uiState.asStateFlow()

    fun onShippingMethodSelected(method: String) {
        _uiState.update { it.copy(shippingMethod = method) }
    }

    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.updateShippingMethod(_uiState.value.shippingMethod)
            onSaved()
        }
    }
}
