package com.example.myappmobile.data.repository

import android.app.ActivityManager
import android.util.Log
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.domain.model.AppNotification
import com.example.myappmobile.domain.model.NotificationType
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FloraFirebaseMessagingService : FirebaseMessagingService() {
    private val timestampFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm")

    override fun onNewToken(token: String) {
        AppContainer.notificationRepository.updateCurrentDeviceToken(token)
        val userId = AppContainer.authRepository.currentUser.value.id
        if (userId.isNotBlank()) {
            AppContainer.notificationRepository.registerCurrentDevice(userId)
            AppContainer.notificationBackendApi.registerDeviceToken(userId, token)
                .onFailure { error -> Log.w(TAG, "Failed to sync FCM token", error) }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val userId = AppContainer.authRepository.currentUser.value.id
        val orderId = message.data["orderId"].orEmpty()
        val title = message.notification?.title ?: message.data["title"].orEmpty()
        val body = message.notification?.body ?: message.data["body"].orEmpty()

        if (userId.isNotBlank() && title.isNotBlank() && body.isNotBlank()) {
            AppContainer.notificationRepository.saveNotification(
                AppNotification(
                    id = "notification_${orderId.ifBlank { System.currentTimeMillis().toString() }}",
                    userId = userId,
                    title = title,
                    body = body,
                    type = NotificationType.ORDER_DELIVERED,
                    relatedOrderId = orderId,
                    createdAt = LocalDateTime.now().format(timestampFormatter),
                ),
            )
        }

        if (isAppInForeground() && title.isNotBlank() && body.isNotBlank()) {
            AppContainer.localNotificationGateway.sendDeliveredNotification(
                title = title,
                body = body,
                orderId = orderId.ifBlank { System.currentTimeMillis().toString() },
            )
        }
    }

    private fun isAppInForeground(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        return manager.runningAppProcesses?.any { process ->
            process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                process.processName == packageName
        } == true
    }

    private companion object {
        const val TAG = "FloraFcmService"
    }
}
