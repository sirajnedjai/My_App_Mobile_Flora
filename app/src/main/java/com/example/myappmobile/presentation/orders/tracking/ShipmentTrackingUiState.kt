package com.example.myappmobile.presentation.orders.tracking

import com.example.myappmobile.domain.model.Order

data class ShipmentTrackingUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val stages: List<ShipmentTrackingStage> = emptyList(),
    val errorMessage: String? = null,
)

data class ShipmentTrackingStage(
    val title: String,
    val subtitle: String,
    val state: ShipmentTrackingStageState,
)

enum class ShipmentTrackingStageState {
    UPCOMING,
    CURRENT,
    COMPLETED,
    CANCELLED,
}
