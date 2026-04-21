package com.example.myappmobile.presentation.checkout.payment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.toApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {
    private companion object {
        const val TAG = "PaymentViewModel"
    }

    private val repository = AppContainer.orderRepository
    private val createOrderUseCase = AppContainer.createOrderUseCase

    private val _uiState = MutableStateFlow(
        PaymentUiState(
            paymentMethod = repository.checkoutDraft.value.paymentMethod,
            subtotal = AppContainer.cartRepository.cartItems.value.sumOf { it.product.price * it.quantity },
            itemCount = AppContainer.cartRepository.cartItems.value.sumOf { it.quantity },
        )
    )
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun onPaymentMethodSelected(method: String) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun placeOrder(onPlaced: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                repository.updatePaymentMethod(_uiState.value.paymentMethod)
                createOrderUseCase()
            }.onSuccess {
                onPlaced()
            }.onFailure { error ->
                Log.d(TAG, "Payment checkout failed. error=${error.toApiException().message}")
            }
        }
    }
}
