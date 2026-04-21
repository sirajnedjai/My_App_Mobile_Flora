package com.example.myappmobile.presentation.seller.orders

import com.example.myappmobile.domain.model.Order
import com.example.myappmobile.domain.model.OrderStatus

data class SellerOrderDetailUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val availableStatuses: List<OrderStatus> = listOf(
        OrderStatus.PENDING,
        OrderStatus.CONFIRMED,
        OrderStatus.SHIPPED,
        OrderStatus.DELIVERED,
        OrderStatus.CANCELLED,
    ),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isUpdatingStatus: Boolean = false,
)
