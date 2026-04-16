package com.example.myappmobile.presentation.seller.orders

import com.example.myappmobile.domain.model.Order
import com.example.myappmobile.domain.model.User

data class SellerOrdersUiState(
    val user: User? = null,
    val isSeller: Boolean = false,
    val orders: List<Order> = emptyList(),
)
