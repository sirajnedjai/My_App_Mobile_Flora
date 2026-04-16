package com.example.myappmobile.data.repository

import com.example.myappmobile.data.mapper.ProductMapper
import com.example.myappmobile.domain.Product
import com.example.myappmobile.domain.model.CartItem
import com.example.myappmobile.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CartRepositoryImpl : CartRepository {

    private val _cartItems = MutableStateFlow(emptyList<CartItem>())
    override val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    override suspend fun addToCart(product: Product) {
        _cartItems.update { current ->
            val existingItem = current.firstOrNull { it.product.id == product.id }
            if (existingItem == null) {
                current + ProductMapper.toCartItem(product)
            } else {
                current.map { item ->
                    if (item.product.id == product.id) item.copy(quantity = item.quantity + 1) else item
                }
            }
        }
    }

    override suspend fun increaseQuantity(productId: String) {
        _cartItems.update { current ->
            current.map { item ->
                if (item.product.id == productId) item.copy(quantity = item.quantity + 1) else item
            }
        }
    }

    override suspend fun decreaseQuantity(productId: String) {
        _cartItems.update { current ->
            current.map { item ->
                if (item.product.id == productId) item.copy(quantity = (item.quantity - 1).coerceAtLeast(1)) else item
            }
        }
    }

    override suspend fun removeFromCart(productId: String) {
        _cartItems.update { current -> current.filterNot { it.product.id == productId } }
    }

    override suspend fun clearCart() {
        _cartItems.value = emptyList()
    }

    override fun getCheckoutItems(): List<CartItem> = cartItems.value
}
