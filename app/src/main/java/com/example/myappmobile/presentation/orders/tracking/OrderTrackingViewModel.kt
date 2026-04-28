package com.example.myappmobile.presentation.orders.tracking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.toApiException
import com.example.myappmobile.domain.model.SellerApprovalStatus
import com.example.myappmobile.domain.model.User
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
            when {
                user.isAuthenticated && user.isSeller -> AppContainer.orderRepository.observeOrdersForSeller(user.id)
                user.isAuthenticated -> {
                    AppContainer.orderRepository.observeOrdersForCustomer(user.id)
                }
                else -> {
                    kotlinx.coroutines.flow.flowOf(emptyList())
                }
            }
        },
        refreshState,
    ) { user, orders, refresh ->
        val safeUser = user.toSafeUiUser()
        OrderTrackingUiState(
            isLoading = refresh.isLoading,
            customerName = safeUser.fullName,
            isSellerView = safeUser.isSeller,
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
            if (!user.isAuthenticated) {
                refreshState.value = OrderTrackingUiState(isLoading = false)
                return@launch
            }
            refreshState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = if (user.isSeller) {
                repository.refreshSellerOrders()
            } else {
                repository.refreshCustomerOrders()
            }
            result
                .onSuccess {
                    Log.d(TAG, "Orders refreshed successfully. seller=${user.isSeller}")
                    refreshState.update { it.copy(isLoading = false, errorMessage = null) }
                }
                .onFailure { error ->
                    val apiError = error.toApiException()
                    Log.d(TAG, "Orders refresh failed. seller=${user.isSeller} error=${apiError.message}")
                    refreshState.update { it.copy(isLoading = false, errorMessage = apiError.message) }
                }
        }
    }

    private fun User.toSafeUiUser() = copy(
        fullName = fullName.orEmpty(),
        email = email.orEmpty(),
        phone = phone.orEmpty(),
        address = address.orEmpty(),
        avatarUrl = avatarUrl.orEmpty(),
        role = role.orEmpty(),
        storeName = storeName.orEmpty(),
        verificationStatus = runCatching { verificationStatus }.getOrDefault(SellerApprovalStatus.NOT_VERIFIED),
        sellerApprovalStatus = runCatching { sellerApprovalStatus }.getOrDefault(SellerApprovalStatus.NOT_VERIFIED),
        membershipTier = membershipTier.orEmpty(),
    )
}
