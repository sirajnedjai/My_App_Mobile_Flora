package com.example.myappmobile.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {

    private val cartRepository = AppContainer.cartRepository
    private val getCartItemsUseCase = AppContainer.getCartItemsUseCase
    private val removeFromCartUseCase = AppContainer.removeFromCartUseCase
    private val promoCode = MutableStateFlow("")
    private val promoApplied = MutableStateFlow(false)
    private val promoMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CartUiState> = combine(
        getCartItemsUseCase(),
        promoCode,
        promoApplied,
        promoMessage,
    ) { items, code, applied, message ->
        CartUiState(
            items = items,
            promoCode = code,
            promoApplied = applied && items.isNotEmpty(),
            promoMessage = message,
        )
    }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CartUiState(),
        )

    fun onIncreaseQuantity(productId: String) {
        viewModelScope.launch {
            cartRepository.increaseQuantity(productId)
        }
    }

    fun onDecreaseQuantity(productId: String) {
        viewModelScope.launch {
            cartRepository.decreaseQuantity(productId)
        }
    }

    fun onRemoveItem(productId: String) {
        viewModelScope.launch {
            removeFromCartUseCase(productId)
        }
    }

    fun onPromoCodeChange(value: String) {
        promoCode.value = value
        promoApplied.value = false
        promoMessage.value = null
    }

    fun onApplyPromoCode() {
        val normalizedCode = promoCode.value.trim().uppercase()
        if (uiState.value.items.isEmpty()) {
            promoApplied.value = false
            promoMessage.value = "Add an item before applying a code."
            return
        }

        if (normalizedCode == "FLORA10") {
            promoApplied.value = true
            promoMessage.value = "FLORA10 applied. You saved 10%."
        } else {
            promoApplied.value = false
            promoMessage.value = "Invalid code. Try FLORA10."
        }
    }
}
