package com.example.myappmobile.presentation.seller.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.domain.model.OrderStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SellerOrderDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val orderId: String = savedStateHandle.get<String>("orderId").orEmpty()

    val uiState: StateFlow<SellerOrderDetailUiState> = combine(
        AppContainer.authRepository.currentUser,
        AppContainer.orderRepository.orders,
    ) { user, _ ->
        SellerOrderDetailUiState(
            isLoading = false,
            order = AppContainer.orderRepository.getOrderForSeller(user.id, orderId),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SellerOrderDetailUiState(),
    )

    fun updateStatus(status: OrderStatus) {
        val sellerId = AppContainer.authRepository.currentUser.value.id
        viewModelScope.launch {
            AppContainer.orderRepository.updateOrderStatus(
                orderId = orderId,
                sellerId = sellerId,
                newStatus = status,
            ).fold(
                onSuccess = { },
                onFailure = { error ->
                    // A follow-up combine emission will carry the updated order if successful.
                },
            )
        }
    }
}
