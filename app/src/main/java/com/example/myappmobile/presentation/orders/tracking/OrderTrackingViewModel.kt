package com.example.myappmobile.presentation.orders.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class OrderTrackingViewModel : ViewModel() {

    val uiState: StateFlow<OrderTrackingUiState> = combine(
        AppContainer.authRepository.currentUser,
        AppContainer.authRepository.currentUser.flatMapLatest { user ->
            if (user.isAuthenticated && !user.isSeller) {
                AppContainer.orderRepository.observeOrdersForCustomer(user.id)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        },
    ) { user, orders ->
        OrderTrackingUiState(
            isLoading = false,
            customerName = user.fullName,
            orders = orders,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OrderTrackingUiState(),
    )
}
