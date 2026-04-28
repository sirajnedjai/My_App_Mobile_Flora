package com.example.myappmobile.presentation.orders.tracking

import com.example.myappmobile.domain.model.Order

data class OrderTrackingUiState(
    val isLoading: Boolean = true,
    val customerName: String = "",
    val isSellerView: Boolean = false,
    val orders: List<Order> = emptyList(),
    val errorMessage: String? = null,
)
