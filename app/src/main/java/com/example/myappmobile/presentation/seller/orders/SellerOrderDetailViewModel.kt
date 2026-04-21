package com.example.myappmobile.presentation.seller.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.toApiException
import com.example.myappmobile.domain.model.OrderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SellerOrderDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val orderId: String = savedStateHandle.get<String>("orderId").orEmpty()
    private val requestState = MutableStateFlow(SellerOrderDetailUiState(isLoading = true))

    init {
        refresh()
    }

    val uiState: StateFlow<SellerOrderDetailUiState> = combine(
        AppContainer.authRepository.currentUser,
        requestState,
    ) { user, request ->
        request.copy(
            isLoading = request.isLoading,
            order = if (user.isSeller) request.order else null,
            errorMessage = if (user.isSeller) request.errorMessage else "Seller tools are available only for seller accounts.",
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SellerOrderDetailUiState(),
    )

    fun refresh() {
        viewModelScope.launch {
            val user = AppContainer.authRepository.currentUser.value
            Log.d(TAG, "Refreshing seller order details. userId=${user.id} isSeller=${user.isSeller} orderId=$orderId")
            if (!user.isSeller) {
                requestState.value = SellerOrderDetailUiState(
                    isLoading = false,
                    errorMessage = "Seller tools are available only for seller accounts.",
                )
                return@launch
            }
            if (orderId.isBlank()) {
                requestState.value = SellerOrderDetailUiState(
                    isLoading = false,
                    errorMessage = "This order could not be opened.",
                )
                return@launch
            }
            requestState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            AppContainer.orderRepository.fetchSellerOrderDetails(orderId).fold(
                onSuccess = { order ->
                    requestState.update {
                        it.copy(
                            isLoading = false,
                            order = order,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { error ->
                    requestState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toApiException().message,
                        )
                    }
                },
            )
        }
    }

    fun updateStatus(status: OrderStatus) {
        val sellerId = AppContainer.authRepository.currentUser.value.id
        if (orderId.isBlank()) {
            requestState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "This order could not be updated because the id is missing.",
                )
            }
            return
        }
        viewModelScope.launch {
            requestState.update { it.copy(isUpdatingStatus = true, errorMessage = null, successMessage = null) }
            AppContainer.orderRepository.updateOrderStatus(
                orderId = orderId,
                sellerId = sellerId,
                newStatus = status,
            ).fold(
                onSuccess = { updatedOrder ->
                    requestState.update {
                        it.copy(
                            isLoading = false,
                            isUpdatingStatus = false,
                            order = updatedOrder,
                            successMessage = "Order status updated successfully.",
                        )
                    }
                },
                onFailure = { error ->
                    requestState.update {
                        it.copy(
                            isLoading = false,
                            isUpdatingStatus = false,
                            errorMessage = error.toApiException().message,
                        )
                    }
                },
            )
        }
    }

    private companion object {
        const val TAG = "SellerOrderDetailVM"
    }
}
