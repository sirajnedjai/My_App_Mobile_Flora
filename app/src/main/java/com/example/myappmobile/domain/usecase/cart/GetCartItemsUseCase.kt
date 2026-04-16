package com.example.myappmobile.domain.usecase.cart

import com.example.myappmobile.domain.model.CartItem
import com.example.myappmobile.domain.repository.CartRepository
import kotlinx.coroutines.flow.StateFlow

class GetCartItemsUseCase(private val cartRepository: CartRepository) {
    operator fun invoke(): StateFlow<List<CartItem>> = cartRepository.cartItems
}
