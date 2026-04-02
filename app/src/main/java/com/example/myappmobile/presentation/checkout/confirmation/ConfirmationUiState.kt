package com.example.myappmobile.presentation.checkout.confirmation

import com.example.myappmobile.domain.model.Order

data class ConfirmationUiState(
    val order: Order? = null,
)
