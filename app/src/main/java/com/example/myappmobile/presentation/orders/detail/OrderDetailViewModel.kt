package com.example.myappmobile.presentation.orders.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.toApiException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrderDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private companion object {
        const val TAG = "OrderDetailViewModel"
    }

    private val orderId: String = savedStateHandle.get<String>("orderId").orEmpty()
    private val orderRepository = AppContainer.orderRepository

    private val _uiState = MutableStateFlow(
        OrderDetailUiState(
            order = orderRepository.getOrder(orderId),
            isLoading = true,
        ),
    )
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        observeOrderUpdates()
        loadOrder()
    }

    fun retry() {
        loadOrder()
    }

    private fun observeOrderUpdates() {
        viewModelScope.launch {
            orderRepository.orders.collectLatest { orders ->
                val updatedOrder = orders.firstOrNull { it.id == orderId } ?: return@collectLatest
                _uiState.update { state ->
                    if (state.order == null) state else state.copy(order = updatedOrder)
                }
            }
        }
    }

    private fun loadOrder() {
        viewModelScope.launch {
            if (orderId.isBlank()) {
                Log.d(TAG, "Order detail screen opened without an order id.")
                _uiState.value = OrderDetailUiState(
                    isLoading = false,
                    errorMessage = "This order could not be opened.",
                )
                return@launch
            }
            Log.d(TAG, "Loading buyer order details. orderId=$orderId")
            _uiState.update { it.copy(isLoading = true, errorMessage = null, order = it.order ?: orderRepository.getOrder(orderId)) }
            orderRepository.fetchOrderDetails(orderId)
                .onSuccess { order ->
                    Log.d(TAG, "Buyer order details mapped. orderId=${order.id} items=${order.items.size}")
                    _uiState.value = OrderDetailUiState(
                        isLoading = false,
                        order = order,
                    )
                }
                .onFailure { error ->
                    val apiError = error.toApiException()
                    Log.d(TAG, "Buyer order details failed. orderId=$orderId error=${apiError.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = apiError.message,
                        )
                    }
                }
        }
    }
}
