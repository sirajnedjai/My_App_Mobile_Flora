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
    private val initialShippingMethod = repository.checkoutDraft.value.shippingMethod
    private val initialSubtotal = AppContainer.cartRepository.cartItems.value.sumOf { it.product.price * it.quantity }
    private val initialShippingCost = shippingCostFor(initialShippingMethod)

    private val _uiState = MutableStateFlow(
        PaymentUiState(
            paymentMethod = repository.checkoutDraft.value.paymentMethod,
            subtotal = initialSubtotal,
            itemCount = AppContainer.cartRepository.cartItems.value.sumOf { it.quantity },
            shippingMethod = initialShippingMethod,
            shippingCost = initialShippingCost,
            total = initialSubtotal + initialShippingCost,
        )
    )
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun onPaymentMethodSelected(method: String) {
        _uiState.update { it.copy(paymentMethod = method, errorMessage = null) }
    }

    fun placeOrder(onPlaced: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPlacingOrder = true, errorMessage = null) }
            runCatching {
                repository.updatePaymentMethod(_uiState.value.paymentMethod)
                createOrderUseCase()
            }.onSuccess {
                _uiState.update { it.copy(isPlacingOrder = false, errorMessage = null) }
                onPlaced()
            }.onFailure { error ->
                val apiError = error.toApiException()
                Log.d(TAG, "Payment checkout failed. error=${apiError.message}")
                _uiState.update {
                    it.copy(
                        isPlacingOrder = false,
                        errorMessage = apiError.message,
                    )
                }
            }
        }
    }

    private fun shippingCostFor(method: String): Double = when (method) {
        "office_pickup" -> 150.0
        else -> 300.0
    }
}
