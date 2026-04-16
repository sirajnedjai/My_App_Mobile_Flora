package com.example.myappmobile.presentation.seller.orders

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
class SellerOrdersViewModel : ViewModel() {

    private val authRepository = AppContainer.authRepository
    private val repository = AppContainer.orderRepository

    val uiState: StateFlow<SellerOrdersUiState> = combine(
        authRepository.currentUser,
        authRepository.currentUser.flatMapLatest { user ->
            if (user.isSeller) repository.observeOrdersForSeller(user.id) else kotlinx.coroutines.flow.flowOf(emptyList())
        },
    ) { user, orders ->
        SellerOrdersUiState(
            user = user,
            isSeller = user.isSeller,
            orders = if (user.isSeller) orders else emptyList(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SellerOrdersUiState(),
    )
}
