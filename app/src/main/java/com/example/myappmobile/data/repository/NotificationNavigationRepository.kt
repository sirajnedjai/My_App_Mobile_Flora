package com.example.myappmobile.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NotificationNavigationTarget(
    val orderId: String = "",
    val destination: String = "",
)

class NotificationNavigationRepository {
    private val _pendingTarget = MutableStateFlow<NotificationNavigationTarget?>(null)
    val pendingTarget: StateFlow<NotificationNavigationTarget?> = _pendingTarget.asStateFlow()

    fun setPendingOrderNavigation(orderId: String) {
        if (orderId.isBlank()) return
        _pendingTarget.value = NotificationNavigationTarget(
            orderId = orderId,
            destination = "order_tracking",
        )
    }

    fun consume() {
        _pendingTarget.update { null }
    }
}
