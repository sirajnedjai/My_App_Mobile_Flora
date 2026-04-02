package com.example.myappmobile.domain.usecase.cart

import com.example.myappmobile.domain.repository.CartRepository

class GetCartItemsUseCase(private val cartRepository: CartRepository) {
    operator fun invoke() = cartRepository.cartItems
}
