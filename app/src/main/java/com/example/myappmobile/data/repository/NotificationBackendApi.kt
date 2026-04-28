package com.example.myappmobile.data.repository

import android.util.Log
import com.example.myappmobile.BuildConfig
import com.example.myappmobile.data.remote.BackendUrlResolver
import com.example.myappmobile.domain.model.AppNotification
import com.example.myappmobile.domain.model.NotificationType
import com.example.myappmobile.domain.model.Order
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import org.json.JSONArray
import org.json.JSONObject

class NotificationBackendApi {

    fun registerDeviceToken(
        userId: String,
        token: String,
    ): Result<Unit> {
        if (userId.isBlank() || token.isBlank()) return Result.success(Unit)
        if (baseUrl.isBlank()) {
            Log.w(TAG, "Skipping device token sync because FLORA_NOTIFICATION_API_BASE_URL is not configured.")
            return Result.success(Unit)
        }
        return post(
            path = "/devices/register",
            body = JSONObject()
                .put("userId", userId)
                .put("token", token)
                .put("platform", "android"),
        )
    }

    fun sendOrderDeliveredNotification(
        userId: String,
        order: Order,
    ): Result<Unit> {
        if (userId.isBlank()) return Result.success(Unit)
        if (baseUrl.isBlank()) {
            return Result.failure(IllegalStateException("FLORA_NOTIFICATION_API_BASE_URL is not configured."))
        }
        val firstProductName = order.items.firstOrNull()?.product?.name.orEmpty()
        return post(
            path = "/notifications/order-delivered",
            body = JSONObject()
                .put("userId", userId)
                .put("orderId", order.id)
                .put("orderReference", order.reference)
                .put("productName", firstProductName)
                .put("notification", JSONObject()
                    .put("title", "Order Delivered")
                    .put("body", "Your FLORA order ${order.reference} has been delivered."))
                .put("data", JSONObject()
                    .put("orderId", order.id)
                    .put("type", "ORDER_DELIVERED"))
                .put("priority", "high"),
        )
    }

    fun sendSellerOrderPlacedNotification(
        sellerId: String,
        order: Order,
    ): Result<Unit> {
        if (sellerId.isBlank()) return Result.success(Unit)
        if (baseUrl.isBlank()) {
            return Result.failure(IllegalStateException("FLORA_NOTIFICATION_API_BASE_URL is not configured."))
        }
        val itemCount = order.items.sumOf { it.quantity }
        return post(
            path = "/notifications/seller/order-placed",
            body = JSONObject()
                .put("userId", sellerId)
                .put("orderId", order.id)
                .put("orderReference", order.reference)
                .put("customerName", order.customerName)
                .put("notification", JSONObject()
                    .put("title", "New Order Received")
                    .put("body", "${order.customerName.ifBlank { "A buyer" }} placed an order with $itemCount item(s)."))
                .put("data", JSONObject()
                    .put("orderId", order.id)
                    .put("type", "SELLER_ORDER_PLACED"))
                .put("priority", "high"),
        )
    }

    fun sendSellerReviewNotification(
        sellerId: String,
        productId: String,
        productName: String,
        reviewerName: String,
        reviewSnippet: String,
    ): Result<Unit> {
        if (sellerId.isBlank()) return Result.success(Unit)
        if (baseUrl.isBlank()) {
            return Result.failure(IllegalStateException("FLORA_NOTIFICATION_API_BASE_URL is not configured."))
        }
        return post(
            path = "/notifications/seller/review",
            body = JSONObject()
                .put("userId", sellerId)
                .put("productId", productId)
                .put("notification", JSONObject()
                    .put("title", "New Review On Your Product")
                    .put("body", "${reviewerName.ifBlank { "A buyer" }} reviewed ${productName.ifBlank { "your product" }}."))
                .put("data", JSONObject()
                    .put("productId", productId)
                    .put("type", "SELLER_REVIEW_RECEIVED")
                    .put("reviewSnippet", reviewSnippet))
                .put("priority", "high"),
        )
    }

    fun fetchNotifications(userId: String): Result<List<AppNotification>> {
        if (userId.isBlank() || baseUrl.isBlank()) return Result.success(emptyList())
        return runCatching {
            withRetry(method = "GET", path = "/notifications?userId=$userId") { connection ->
                if (connection.responseCode !in 200..299) {
                    error("Request failed with ${connection.responseCode}")
                }
                connection.inputStream.bufferedReader().use { reader ->
                    val raw = reader.readText()
                    val root = JSONArray(raw)
                    buildList {
                        for (index in 0 until root.length()) {
                            val item = root.getJSONObject(index)
                            add(
                                AppNotification(
                                    id = item.getString("id"),
                                    userId = item.getString("userId"),
                                    title = item.getString("title"),
                                    body = item.getString("body"),
                                    type = runCatching {
                                        NotificationType.valueOf(item.optString("type", "ORDER_DELIVERED"))
                                    }.getOrDefault(NotificationType.ORDER_DELIVERED),
                                    relatedOrderId = item.optString("relatedOrderId"),
                                    isRead = item.optBoolean("isRead", false),
                                    createdAt = item.optString("createdAt"),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun post(
        path: String,
        body: JSONObject,
    ): Result<Unit> = runCatching {
        withRetry(method = "POST", path = path) { connection ->
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.bufferedWriter().use { writer ->
                writer.write(body.toString())
            }
            if (connection.responseCode !in 200..299) {
                error("Request failed with ${connection.responseCode}")
            }
        }
    }

    private val baseUrl: String
        get() = BuildConfig.FLORA_NOTIFICATION_API_BASE_URL
            .trim()
            .ifBlank { BackendUrlResolver.originUrl }
            .removeSuffix("/api")
            .trimEnd('/')

    private fun openConnection(
        path: String,
        method: String,
    ): HttpURLConnection = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
        requestMethod = method
        connectTimeout = CONNECT_TIMEOUT_MS
        readTimeout = READ_TIMEOUT_MS
        setRequestProperty("Accept", "application/json")
        useCaches = false
    }

    private fun <T> withRetry(
        method: String,
        path: String,
        action: (HttpURLConnection) -> T,
    ): T {
        var attempt = 0
        var lastError: Exception? = null

        while (attempt < MAX_ATTEMPTS) {
            attempt += 1
            try {
                val connection = openConnection(path = path, method = method)
                val result = action(connection)
                if (connection.responseCode !in RETRIABLE_STATUS_CODES || attempt >= MAX_ATTEMPTS) {
                    return result
                }
            } catch (error: Exception) {
                lastError = error
                val isRetriable = error is IOException || error is SocketTimeoutException || error is UnknownHostException
                if (!isRetriable || attempt >= MAX_ATTEMPTS) throw error
            }

            Thread.sleep(RETRY_BACKOFF_MS * attempt)
        }

        throw lastError ?: IllegalStateException("Notification request failed.")
    }

    private companion object {
        const val TAG = "NotificationBackendApi"
        const val CONNECT_TIMEOUT_MS = 20_000
        const val READ_TIMEOUT_MS = 90_000
        const val RETRY_BACKOFF_MS = 1_500L
        const val MAX_ATTEMPTS = 3
        val RETRIABLE_STATUS_CODES = setOf(408, 425, 429, 500, 502, 503, 504)
    }
}
