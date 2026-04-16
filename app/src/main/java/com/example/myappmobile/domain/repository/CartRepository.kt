package com.example.myappmobile.domain.repository

import com.example.myappmobile.domain.Product
import com.example.myappmobile.domain.model.CartItem
import kotlinx.coroutines.flow.StateFlow

interface CartRepository {
    val cartItems: StateFlow<List<CartItem>>

    suspend fun addToCart(product: Product)

    suspend fun increaseQuantity(productId: String)

    suspend fun decreaseQuantity(productId: String)

    suspend fun removeFromCart(productId: String)

    suspend fun clearCart()

    fun getCheckoutItems(): List<CartItem>
}
