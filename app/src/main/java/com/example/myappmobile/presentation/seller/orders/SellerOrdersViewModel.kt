package com.example.myappmobile.presentation.seller.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import android.util.Log
import com.example.myappmobile.data.remote.toApiException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SellerOrdersViewModel : ViewModel() {

    private val authRepository = AppContainer.authRepository
    private val repository = AppContainer.orderRepository
    private val requestState = MutableStateFlow(SellerOrdersUiState(isLoading = true))

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                Log.d(TAG, "Seller orders screen user state. userId=${user.id} isSeller=${user.isSeller}")
                if (!user.isSeller) {
                    requestState.value = SellerOrdersUiState(isLoading = false)
                    return@collect
                }
                performRefresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            performRefresh()
        }
    }

    val uiState: StateFlow<SellerOrdersUiState> = combine(
        authRepository.currentUser,
        authRepository.currentUser.flatMapLatest { user ->
            if (user.isSeller) repository.observeOrdersForSeller(user.id) else kotlinx.coroutines.flow.flowOf(emptyList())
        },
        requestState,
    ) { user, orders, request ->
        request.copy(
            user = user,
            isSeller = user.isSeller,
            orders = if (user.isSeller) orders else emptyList(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SellerOrdersUiState(),
    )

    private companion object {
        const val TAG = "SellerOrdersViewModel"
    }

    private suspend fun performRefresh() {
        val user = authRepository.currentUser.value
        if (!user.isSeller) {
            requestState.value = SellerOrdersUiState(isLoading = false)
            return
        }
        requestState.update { it.copy(isLoading = true, errorMessage = null) }
        repository.refreshSellerOrders().fold(
            onSuccess = {
                requestState.update { it.copy(isLoading = false, errorMessage = null) }
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
