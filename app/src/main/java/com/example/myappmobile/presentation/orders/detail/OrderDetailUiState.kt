package com.example.myappmobile.presentation.orders.detail

import com.example.myappmobile.domain.model.Order

data class OrderDetailUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val errorMessage: String? = null,
)
