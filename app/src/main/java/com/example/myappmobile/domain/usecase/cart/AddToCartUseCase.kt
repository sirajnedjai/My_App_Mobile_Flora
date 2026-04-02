package com.example.myappmobile.domain.usecase.cart

import com.example.myappmobile.domain.Product
import com.example.myappmobile.domain.repository.CartRepository

class AddToCartUseCase(private val cartRepository: CartRepository) {
    suspend operator fun invoke(product: Product) = cartRepository.addToCart(product)
}
