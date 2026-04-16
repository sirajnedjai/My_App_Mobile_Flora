package com.example.myappmobile.data.repository

import android.util.Log
import com.example.myappmobile.domain.model.Order

class OrderNotificationService(
    private val notificationBackendApi: NotificationBackendApi,
    private val accountSettingsRepository: AccountSettingsRepository,
) {
    fun sendOrderDeliveredNotification(
        userId: String,
        order: Order,
    ) {
        if (userId.isBlank()) return
        val settings = accountSettingsRepository.getNotificationSettings(userId)
        if (!settings.orderUpdates.pushEnabled) return

        notificationBackendApi.sendOrderDeliveredNotification(
            userId = userId,
            order = order,
        ).onFailure { error ->
            Log.w(TAG, "Failed to request backend FCM delivery for order ${order.id}", error)
        }
    }

    private companion object {
        const val TAG = "OrderNotificationSvc"
    }
}
