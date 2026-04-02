package com.example.myappmobile.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {

    private val getCartItemsUseCase = AppContainer.getCartItemsUseCase
    private val removeFromCartUseCase = AppContainer.removeFromCartUseCase

    val uiState: StateFlow<CartUiState> = getCartItemsUseCase()
        .map { items -> CartUiState(items = items) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CartUiState(),
        )

    fun onRemoveItem(productId: String) {
        viewModelScope.launch {
            removeFromCartUseCase(productId)
        }
    }
}
