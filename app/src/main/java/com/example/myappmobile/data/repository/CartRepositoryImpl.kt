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

    private val _cartItems = MutableStateFlow(emptyList<Product>())
    override val cartItems: StateFlow<List<Product>> = _cartItems.asStateFlow()

    override suspend fun addToCart(product: Product) {
        _cartItems.update { current ->
            if (current.any { it.id == product.id }) current else current + product
        }
    }

    override suspend fun removeFromCart(productId: String) {
        _cartItems.update { current -> current.filterNot { it.id == productId } }
    }

    override suspend fun clearCart() {
        _cartItems.value = emptyList()
    }

    override fun getCheckoutItems(): List<CartItem> = cartItems.value.map(ProductMapper::toCartItem)
}
