package com.example.myappmobile.domain.usecase.cart

import com.example.myappmobile.domain.repository.CartRepository

class RemoveFromCartUseCase(private val cartRepository: CartRepository) {
    suspend operator fun invoke(productId: String) = cartRepository.removeFromCart(productId)
}
