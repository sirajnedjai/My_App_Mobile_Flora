package com.example.myappmobile.presentation.orders.tracking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.toApiException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class OrderTrackingViewModel : ViewModel() {
    private companion object {
        const val TAG = "OrderTrackingViewModel"
    }

    private val repository = AppContainer.orderRepository
    private val refreshState = MutableStateFlow(OrderTrackingUiState())

    init {
        refresh()
    }

    val uiState: StateFlow<OrderTrackingUiState> = combine(
        AppContainer.authRepository.currentUser,
        AppContainer.authRepository.currentUser.flatMapLatest { user ->
            if (user.isAuthenticated && !user.isSeller) {
                AppContainer.orderRepository.observeOrdersForCustomer(user.id)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        },
        refreshState,
    ) { user, orders, refresh ->
        OrderTrackingUiState(
            isLoading = refresh.isLoading,
            customerName = user.fullName,
            orders = orders,
            errorMessage = refresh.errorMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OrderTrackingUiState(),
    )

    fun refresh() {
        viewModelScope.launch {
            val user = AppContainer.authRepository.currentUser.value
            if (!user.isAuthenticated || user.isSeller) {
                refreshState.value = OrderTrackingUiState(isLoading = false)
                return@launch
            }
            refreshState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.refreshCustomerOrders()
                .onSuccess {
                    Log.d(TAG, "Buyer orders refreshed successfully.")
                    refreshState.update { it.copy(isLoading = false, errorMessage = null) }
                }
                .onFailure { error ->
                    val apiError = error.toApiException()
                    Log.d(TAG, "Buyer orders refresh failed. error=${apiError.message}")
                    refreshState.update { it.copy(isLoading = false, errorMessage = apiError.message) }
                }
        }
    }
}
