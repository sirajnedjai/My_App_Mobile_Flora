package com.example.myappmobile.domain.model

data class AppNotification(
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val relatedOrderId: String = "",
    val isRead: Boolean = false,
    val createdAt: String,
)

enum class NotificationType {
    ORDER_DELIVERED,
}

data class DeviceRegistration(
    val token: String,
    val userId: String,
    val platform: String = "android",
    val updatedAt: String,
)
