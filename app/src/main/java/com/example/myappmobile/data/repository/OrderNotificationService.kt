package com.example.myappmobile.data.repository

import android.util.Log
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.domain.model.AppNotification
import com.example.myappmobile.domain.model.NotificationType
import com.example.myappmobile.domain.model.Order
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OrderNotificationService(
    private val notificationBackendApi: NotificationBackendApi,
    private val accountSettingsRepository: AccountSettingsRepository,
) {
    private val timestampFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm")

    fun sendOrderDeliveredNotification(
        userId: String,
        order: Order,
    ) {
        if (userId.isBlank()) return
        val settings = accountSettingsRepository.getNotificationSettings(userId)
        if (!settings.orderUpdates.pushEnabled) return

        AppContainer.notificationRepository.saveNotification(
            AppNotification(
                id = "order_delivered_${order.id}",
                userId = userId,
                title = "Order Delivered",
                body = "Your FLORA order ${order.reference} has been delivered.",
                type = NotificationType.ORDER_DELIVERED,
                relatedOrderId = order.id,
                createdAt = LocalDateTime.now().format(timestampFormatter),
            ),
        )

        notificationBackendApi.sendOrderDeliveredNotification(
            userId = userId,
            order = order,
        ).onFailure { error ->
            Log.w(TAG, "Failed to request backend FCM delivery for order ${order.id}", error)
        }
    }

    fun sendSellerOrderPlacedNotification(
        sellerId: String,
        order: Order,
    ) {
        if (sellerId.isBlank()) return
        val settings = accountSettingsRepository.getNotificationSettings(sellerId)
        if (!settings.orderUpdates.pushEnabled) return

        AppContainer.notificationRepository.saveNotification(
            AppNotification(
                id = "seller_order_${sellerId}_${order.id}",
                userId = sellerId,
                title = "New Order Received",
                body = "${order.customerName.ifBlank { "A buyer" }} placed order ${order.reference}.",
                type = NotificationType.SELLER_ORDER_PLACED,
                relatedOrderId = order.id,
                createdAt = LocalDateTime.now().format(timestampFormatter),
            ),
        )

        notificationBackendApi.sendSellerOrderPlacedNotification(
            sellerId = sellerId,
            order = order,
        ).onFailure { error ->
            Log.w(TAG, "Failed to request seller new-order notification for ${order.id}", error)
        }
    }

    fun sendSellerReviewNotification(
        sellerId: String,
        productId: String,
        productName: String,
        reviewerName: String,
        reviewSnippet: String,
    ) {
        if (sellerId.isBlank()) return
        val settings = accountSettingsRepository.getNotificationSettings(sellerId)
        if (!settings.newArrivals.pushEnabled) return

        AppContainer.notificationRepository.saveNotification(
            AppNotification(
                id = "seller_review_${sellerId}_${productId}_${System.currentTimeMillis()}",
                userId = sellerId,
                title = "New Review On Your Product",
                body = "${reviewerName.ifBlank { "A buyer" }} reviewed ${productName.ifBlank { "your product" }}.",
                type = NotificationType.SELLER_REVIEW_RECEIVED,
                createdAt = LocalDateTime.now().format(timestampFormatter),
            ),
        )

        notificationBackendApi.sendSellerReviewNotification(
            sellerId = sellerId,
            productId = productId,
            productName = productName,
            reviewerName = reviewerName,
            reviewSnippet = reviewSnippet,
        ).onFailure { error ->
            Log.w(TAG, "Failed to request seller review notification for product $productId", error)
        }
    }

    private companion object {
        const val TAG = "OrderNotificationSvc"
    }
}
