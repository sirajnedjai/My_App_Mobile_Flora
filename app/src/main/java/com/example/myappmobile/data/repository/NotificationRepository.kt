package com.example.myappmobile.data.repository

import android.content.Context
import com.example.myappmobile.domain.model.AppNotification
import com.example.myappmobile.domain.model.DeviceRegistration
import com.example.myappmobile.domain.model.NotificationType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NotificationRepository {
    private var appContext: Context? = null
    private val timestampFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm")

    private val _notificationsByUser = MutableStateFlow<Map<String, List<AppNotification>>>(emptyMap())
    val notificationsByUser: StateFlow<Map<String, List<AppNotification>>> = _notificationsByUser.asStateFlow()

    private val _deviceRegistrationsByUser = MutableStateFlow<Map<String, List<DeviceRegistration>>>(emptyMap())
    val deviceRegistrationsByUser: StateFlow<Map<String, List<DeviceRegistration>>> = _deviceRegistrationsByUser.asStateFlow()

    private val _currentDeviceToken = MutableStateFlow("")
    val currentDeviceToken: StateFlow<String> = _currentDeviceToken.asStateFlow()

    fun initialize(context: Context) {
        appContext = context.applicationContext
        val prefs = prefs()
        _notificationsByUser.value = decodeNotifications(prefs.getString(KEY_NOTIFICATIONS, null))
        _deviceRegistrationsByUser.value = decodeDeviceRegistrations(prefs.getString(KEY_DEVICE_REGISTRATIONS, null))
        _currentDeviceToken.value = prefs.getString(KEY_CURRENT_DEVICE_TOKEN, null).orEmpty()
    }

    fun getNotifications(userId: String): List<AppNotification> = notificationsByUser.value[userId].orEmpty()

    fun saveNotification(notification: AppNotification) {
        val existing = getNotifications(notification.userId)
        val updated = (listOf(notification) + existing).distinctBy(AppNotification::id)
        persistNotifications(_notificationsByUser.value + (notification.userId to updated))
    }

    fun markAsRead(userId: String, notificationId: String) {
        val updated = getNotifications(userId).map { notification ->
            if (notification.id == notificationId) notification.copy(isRead = true) else notification
        }
        persistNotifications(_notificationsByUser.value + (userId to updated))
    }

    fun markAllAsRead(userId: String) {
        val updated = getNotifications(userId).map { it.copy(isRead = true) }
        persistNotifications(_notificationsByUser.value + (userId to updated))
    }

    fun registerCurrentDevice(userId: String) {
        if (userId.isBlank()) return
        val token = currentDeviceToken.value.ifBlank { return }
        val registration = DeviceRegistration(
            token = token,
            userId = userId,
            updatedAt = LocalDateTime.now().format(timestampFormatter),
        )
        val current = deviceRegistrationsByUser.value[userId].orEmpty()
            .filterNot { it.token == token }
        persistRegistrations(_deviceRegistrationsByUser.value + (userId to (listOf(registration) + current)))
    }

    fun updateCurrentDeviceToken(token: String) {
        prefs().edit().putString(KEY_CURRENT_DEVICE_TOKEN, token).apply()
        _currentDeviceToken.value = token
    }

    fun getDeviceTokens(userId: String): List<String> = deviceRegistrationsByUser.value[userId]
        .orEmpty()
        .map(DeviceRegistration::token)
        .distinct()

    fun hasOrderDeliveredNotification(userId: String, orderId: String): Boolean =
        getNotifications(userId).any { notification ->
            notification.type == NotificationType.ORDER_DELIVERED && notification.relatedOrderId == orderId
        }

    private fun persistNotifications(notifications: Map<String, List<AppNotification>>) {
        prefs().edit().putString(KEY_NOTIFICATIONS, encodeNotifications(notifications)).apply()
        _notificationsByUser.value = notifications
    }

    private fun persistRegistrations(registrations: Map<String, List<DeviceRegistration>>) {
        prefs().edit().putString(KEY_DEVICE_REGISTRATIONS, encodeDeviceRegistrations(registrations)).apply()
        _deviceRegistrationsByUser.value = registrations
    }

    private fun encodeNotifications(value: Map<String, List<AppNotification>>): String =
        value.entries.joinToString(ENTRY_SEPARATOR) { (userId, notifications) ->
            listOf(
                userId,
                notifications.joinToString(RECORD_SEPARATOR) { notification ->
                    listOf(
                        notification.id,
                        notification.userId,
                        notification.title,
                        notification.body,
                        notification.type.name,
                        notification.relatedOrderId,
                        notification.isRead.toString(),
                        notification.createdAt,
                    ).joinToString(FIELD_SEPARATOR, transform = ::escape)
                },
            ).joinToString(FIELD_SEPARATOR, transform = ::escape)
        }

    private fun decodeNotifications(raw: String?): Map<String, List<AppNotification>> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(ENTRY_SEPARATOR).mapNotNull { entry ->
            val parts = entry.split(FIELD_SEPARATOR, limit = 2)
            if (parts.size < 2) return@mapNotNull null
            val userId = unescape(parts[0])
            val notifications = unescape(parts[1]).split(RECORD_SEPARATOR)
                .filter { it.isNotBlank() }
                .mapNotNull { record ->
                    val fields = record.split(FIELD_SEPARATOR).map(::unescape)
                    if (fields.size < 8) return@mapNotNull null
                    AppNotification(
                        id = fields[0],
                        userId = fields[1],
                        title = fields[2],
                        body = fields[3],
                        type = runCatching { NotificationType.valueOf(fields[4]) }.getOrNull() ?: return@mapNotNull null,
                        relatedOrderId = fields[5],
                        isRead = fields[6].toBoolean(),
                        createdAt = fields[7],
                    )
                }
            userId to notifications
        }.toMap()
    }

    private fun encodeDeviceRegistrations(value: Map<String, List<DeviceRegistration>>): String =
        value.entries.joinToString(ENTRY_SEPARATOR) { (userId, registrations) ->
            listOf(
                userId,
                registrations.joinToString(RECORD_SEPARATOR) { registration ->
                    listOf(
                        registration.token,
                        registration.userId,
                        registration.platform,
                        registration.updatedAt,
                    ).joinToString(FIELD_SEPARATOR, transform = ::escape)
                },
            ).joinToString(FIELD_SEPARATOR, transform = ::escape)
        }

    private fun decodeDeviceRegistrations(raw: String?): Map<String, List<DeviceRegistration>> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(ENTRY_SEPARATOR).mapNotNull { entry ->
            val parts = entry.split(FIELD_SEPARATOR, limit = 2)
            if (parts.size < 2) return@mapNotNull null
            val userId = unescape(parts[0])
            val registrations = unescape(parts[1]).split(RECORD_SEPARATOR)
                .filter { it.isNotBlank() }
                .mapNotNull { record ->
                    val fields = record.split(FIELD_SEPARATOR).map(::unescape)
                    if (fields.size < 4) return@mapNotNull null
                    DeviceRegistration(
                        token = fields[0],
                        userId = fields[1],
                        platform = fields[2],
                        updatedAt = fields[3],
                    )
                }
            userId to registrations
        }.toMap()
    }

    private fun prefs() = checkNotNull(appContext) {
        "NotificationRepository is not initialized. Call initialize(context) first."
    }.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun escape(value: String): String = value
        .replace("\\", "\\\\")
        .replace(FIELD_SEPARATOR, ESCAPED_FIELD_SEPARATOR)
        .replace(ENTRY_SEPARATOR, ESCAPED_ENTRY_SEPARATOR)
        .replace(RECORD_SEPARATOR, ESCAPED_RECORD_SEPARATOR)

    private fun unescape(value: String): String = value
        .replace(ESCAPED_FIELD_SEPARATOR, FIELD_SEPARATOR)
        .replace(ESCAPED_ENTRY_SEPARATOR, ENTRY_SEPARATOR)
        .replace(ESCAPED_RECORD_SEPARATOR, RECORD_SEPARATOR)
        .replace("\\\\", "\\")

    private companion object {
        const val PREFS_NAME = "flora_notifications"
        const val KEY_NOTIFICATIONS = "notifications"
        const val KEY_DEVICE_REGISTRATIONS = "device_registrations"
        const val KEY_CURRENT_DEVICE_TOKEN = "current_device_token"
        const val FIELD_SEPARATOR = "\u001F"
        const val ENTRY_SEPARATOR = "\u001E"
        const val RECORD_SEPARATOR = "\u001D"
        const val ESCAPED_FIELD_SEPARATOR = "\\u001F"
        const val ESCAPED_ENTRY_SEPARATOR = "\\u001E"
        const val ESCAPED_RECORD_SEPARATOR = "\\u001D"
    }
}
