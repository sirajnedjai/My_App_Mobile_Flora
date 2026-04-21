package com.example.myappmobile.presentation.orders.tracking

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.toApiException
import com.example.myappmobile.domain.model.Order
import com.example.myappmobile.domain.model.OrderStatus
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShipmentTrackingViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private companion object {
        const val TAG = "ShipmentTrackingVM"
    }

    private val orderId: String = savedStateHandle.get<String>("orderId").orEmpty()
    private val repository = AppContainer.orderRepository

    private val _uiState = MutableStateFlow(ShipmentTrackingUiState())
    val uiState: StateFlow<ShipmentTrackingUiState> = _uiState.asStateFlow()

    init {
        observeOrderUpdates()
        loadTracking()
    }

    fun retry() {
        loadTracking()
    }

    private fun observeOrderUpdates() {
        viewModelScope.launch {
            repository.orders.collectLatest { orders ->
                val updatedOrder = orders.firstOrNull { it.id == orderId } ?: return@collectLatest
                _uiState.update { state ->
                    if (state.order == null) {
                        state
                    } else {
                        state.copy(
                            order = updatedOrder,
                            stages = mapStages(updatedOrder),
                        )
                    }
                }
            }
        }
    }

    private fun loadTracking() {
        viewModelScope.launch {
            if (orderId.isBlank()) {
                Log.d(TAG, "Shipment tracking opened without an order id.")
                _uiState.value = ShipmentTrackingUiState(
                    isLoading = false,
                    errorMessage = "This shipment could not be opened.",
                )
                return@launch
            }
            Log.d(TAG, "Loading shipment tracking. orderId=$orderId")
            _uiState.update { it.copy(isLoading = true, errorMessage = null, order = it.order ?: repository.getOrder(orderId)) }
            repository.fetchOrderDetails(orderId)
                .onSuccess { order ->
                    val stages = mapStages(order)
                    Log.d(TAG, "Tracking order fetched. orderId=${order.id} status=${order.status} stages=${stages.map { it.title to it.state }}")
                    _uiState.value = ShipmentTrackingUiState(
                        isLoading = false,
                        order = order,
                        stages = stages,
                    )
                }
                .onFailure { error ->
                    val apiError = error.toApiException()
                    Log.d(TAG, "Tracking fetch failed. orderId=$orderId error=${apiError.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = apiError.message,
                            stages = it.order?.let(::mapStages).orEmpty(),
                        )
                    }
                }
        }
    }

    private fun mapStages(order: Order): List<ShipmentTrackingStage> {
        val currentStatus = order.status
        if (currentStatus == OrderStatus.CANCELLED) {
            return listOf(
                ShipmentTrackingStage(
                    title = "Order Cancelled",
                    subtitle = order.statusHistory.lastOrNull()?.note?.ifBlank { "This shipment will not proceed." }
                        ?: "This shipment will not proceed.",
                    state = ShipmentTrackingStageState.CANCELLED,
                ),
            )
        }

        val stageDefinitions = listOf(
            OrderStatus.PENDING to ("Order Placed" to "Your order was received and is awaiting confirmation."),
            OrderStatus.CONFIRMED to ("Confirmed" to "Your order has been confirmed and queued for preparation."),
            OrderStatus.HAND_CRAFTED to ("Preparing Shipment" to "The atelier is preparing your order for shipment."),
            OrderStatus.SHIPPED to ("In Transit" to "Your shipment is on the way."),
            OrderStatus.DELIVERED to ("Delivered" to "Your order has been delivered."),
        )

        val currentIndex = when (currentStatus) {
            OrderStatus.PENDING -> 0
            OrderStatus.CONFIRMED -> 1
            OrderStatus.HAND_CRAFTED -> 2
            OrderStatus.SHIPPED -> 3
            OrderStatus.DELIVERED -> 4
            OrderStatus.CANCELLED -> -1
        }

        return stageDefinitions.mapIndexed { index, (_, content) ->
            ShipmentTrackingStage(
                title = content.first,
                subtitle = order.statusHistory.getOrNull(index)?.note?.ifBlank { content.second } ?: content.second,
                state = when {
                    index < currentIndex -> ShipmentTrackingStageState.COMPLETED
                    index == currentIndex -> ShipmentTrackingStageState.CURRENT
                    else -> ShipmentTrackingStageState.UPCOMING
                },
            )
        }
    }
}
