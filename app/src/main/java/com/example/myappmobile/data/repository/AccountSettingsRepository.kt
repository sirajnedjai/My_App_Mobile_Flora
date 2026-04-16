package com.example.myappmobile.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SecurityPreferences(
    val twoFactorEnabled: Boolean = false,
)

data class LoginActivityEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val timestamp: String,
)

data class SavedAddressEntry(
    val id: String,
    val fullName: String,
    val phoneNumber: String,
    val state: String,
    val city: String,
    val postalCode: String,
    val streetAddress: String,
    val label: String,
    val isDefault: Boolean = false,
)

data class PaymentCardEntry(
    val id: String,
    val cardholderName: String,
    val cardNumberMasked: String,
    val expiryDate: String,
    val billingAddress: String,
)

data class NotificationPreference(
    val emailEnabled: Boolean = true,
    val pushEnabled: Boolean = true,
)

data class UserNotificationSettings(
    val orderUpdates: NotificationPreference = NotificationPreference(),
    val promotions: NotificationPreference = NotificationPreference(emailEnabled = true, pushEnabled = false),
    val newArrivals: NotificationPreference = NotificationPreference(emailEnabled = true, pushEnabled = true),
    val wishlistAlerts: NotificationPreference = NotificationPreference(emailEnabled = false, pushEnabled = true),
)

class AccountSettingsRepository {
    private var appContext: Context? = null

    private val _securityPreferences = MutableStateFlow<Map<String, SecurityPreferences>>(emptyMap())
    val securityPreferences: StateFlow<Map<String, SecurityPreferences>> = _securityPreferences.asStateFlow()

    private val _savedAddresses = MutableStateFlow<Map<String, List<SavedAddressEntry>>>(emptyMap())
    val savedAddresses: StateFlow<Map<String, List<SavedAddressEntry>>> = _savedAddresses.asStateFlow()

    private val _paymentMethods = MutableStateFlow<Map<String, List<PaymentCardEntry>>>(emptyMap())
    val paymentMethods: StateFlow<Map<String, List<PaymentCardEntry>>> = _paymentMethods.asStateFlow()

    private val _notificationSettings = MutableStateFlow<Map<String, UserNotificationSettings>>(emptyMap())
    val notificationSettings: StateFlow<Map<String, UserNotificationSettings>> = _notificationSettings.asStateFlow()

    fun initialize(context: Context) {
        appContext = context.applicationContext
        val prefs = prefs()
        _securityPreferences.value = decodeSecurityMap(prefs.getString(KEY_SECURITY, null))
        _savedAddresses.value = decodeAddressMap(prefs.getString(KEY_ADDRESSES, null))
        _paymentMethods.value = decodeCardsMap(prefs.getString(KEY_CARDS, null))
        _notificationSettings.value = decodeNotificationsMap(prefs.getString(KEY_NOTIFICATIONS, null))
    }

    fun getSecurityPreferences(userId: String): SecurityPreferences =
        _securityPreferences.value[userId] ?: SecurityPreferences()

    fun saveSecurityPreferences(userId: String, preferences: SecurityPreferences) {
        val updated = _securityPreferences.value + (userId to preferences)
        prefs().edit().putString(KEY_SECURITY, encodeSecurityMap(updated)).apply()
        _securityPreferences.value = updated
    }

    fun getSavedAddresses(userId: String): List<SavedAddressEntry> = _savedAddresses.value[userId].orEmpty()

    fun saveAddress(userId: String, entry: SavedAddressEntry) {
        val existing = getSavedAddresses(userId)
        val updated = (existing.filterNot { it.id == entry.id } + entry).normalizeDefaults()
        persistAddresses(userId, updated)
    }

    fun setDefaultAddress(userId: String, addressId: String) {
        val updated = getSavedAddresses(userId).map { it.copy(isDefault = it.id == addressId) }
        persistAddresses(userId, updated)
    }

    fun getPaymentMethods(userId: String): List<PaymentCardEntry> = _paymentMethods.value[userId].orEmpty()

    fun savePaymentMethod(userId: String, entry: PaymentCardEntry) {
        val updated = getPaymentMethods(userId).filterNot { it.id == entry.id } + entry
        val persisted = _paymentMethods.value + (userId to updated)
        prefs().edit().putString(KEY_CARDS, encodeCardsMap(persisted)).apply()
        _paymentMethods.value = persisted
    }

    fun getNotificationSettings(userId: String): UserNotificationSettings =
        _notificationSettings.value[userId] ?: UserNotificationSettings()

    fun saveNotificationSettings(userId: String, settings: UserNotificationSettings) {
        val updated = _notificationSettings.value + (userId to settings)
        prefs().edit().putString(KEY_NOTIFICATIONS, encodeNotificationsMap(updated)).apply()
        _notificationSettings.value = updated
    }

    fun loginActivityFor(userName: String): List<LoginActivityEntry> = listOf(
        LoginActivityEntry("1", "Primary device", "Android app", "Today, 09:24"),
        LoginActivityEntry("2", "Recent sign-in", "Algiers, Algeria", "Yesterday, 20:18"),
        LoginActivityEntry("3", "Trusted session", "$userName account", "Apr 10, 2026"),
    )

    private fun persistAddresses(userId: String, entries: List<SavedAddressEntry>) {
        val updated = _savedAddresses.value + (userId to entries.normalizeDefaults())
        prefs().edit().putString(KEY_ADDRESSES, encodeAddressMap(updated)).apply()
        _savedAddresses.value = updated
    }

    private fun List<SavedAddressEntry>.normalizeDefaults(): List<SavedAddressEntry> {
        if (isEmpty()) return emptyList()
        val hasDefault = any { it.isDefault }
        return mapIndexed { index, entry ->
            when {
                entry.isDefault -> entry
                !hasDefault && index == lastIndex -> entry.copy(isDefault = true)
                else -> entry.copy(isDefault = false)
            }
        }
    }

    private fun prefs() = requireNotNull(appContext) {
        "AccountSettingsRepository is not initialized."
    }.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun encodeSecurityMap(value: Map<String, SecurityPreferences>): String =
        value.entries.joinToString(ENTRY_SEPARATOR) { (userId, preferences) ->
            listOf(userId, preferences.twoFactorEnabled.toString()).joinToString(FIELD_SEPARATOR, transform = ::escape)
        }

    private fun decodeSecurityMap(raw: String?): Map<String, SecurityPreferences> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(ENTRY_SEPARATOR).mapNotNull { entry ->
            val fields = entry.split(FIELD_SEPARATOR)
            if (fields.size < 2) return@mapNotNull null
            escapeBack(fields[0]) to SecurityPreferences(escapeBack(fields[1]).toBoolean())
        }.toMap()
    }

    private fun encodeAddressMap(value: Map<String, List<SavedAddressEntry>>): String =
        value.entries.joinToString(ENTRY_SEPARATOR) { (userId, entries) ->
            listOf(userId, entries.joinToString(RECORD_SEPARATOR) { entry ->
                listOf(
                    entry.id,
                    entry.fullName,
                    entry.phoneNumber,
                    entry.state,
                    entry.city,
                    entry.postalCode,
                    entry.streetAddress,
                    entry.label,
                    entry.isDefault.toString(),
                ).joinToString(FIELD_SEPARATOR, transform = ::escape)
            }).joinToString(FIELD_SEPARATOR, transform = ::escape)
        }

    private fun decodeAddressMap(raw: String?): Map<String, List<SavedAddressEntry>> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(ENTRY_SEPARATOR).mapNotNull { entry ->
            val parts = entry.split(FIELD_SEPARATOR, limit = 2)
            if (parts.size < 2) return@mapNotNull null
            val userId = escapeBack(parts[0])
            val addresses = escapeBack(parts[1]).split(RECORD_SEPARATOR).mapNotNull { record ->
                val fields = record.split(FIELD_SEPARATOR).map(::escapeBack)
                if (fields.size < 9) return@mapNotNull null
                SavedAddressEntry(
                    id = fields[0],
                    fullName = fields[1],
                    phoneNumber = fields[2],
                    state = fields[3],
                    city = fields[4],
                    postalCode = fields[5],
                    streetAddress = fields[6],
                    label = fields[7],
                    isDefault = fields[8].toBoolean(),
                )
            }
            userId to addresses
        }.toMap()
    }

    private fun encodeCardsMap(value: Map<String, List<PaymentCardEntry>>): String =
        value.entries.joinToString(ENTRY_SEPARATOR) { (userId, entries) ->
            listOf(userId, entries.joinToString(RECORD_SEPARATOR) { entry ->
                listOf(
                    entry.id,
                    entry.cardholderName,
                    entry.cardNumberMasked,
                    entry.expiryDate,
                    entry.billingAddress,
                ).joinToString(FIELD_SEPARATOR, transform = ::escape)
            }).joinToString(FIELD_SEPARATOR, transform = ::escape)
        }

    private fun decodeCardsMap(raw: String?): Map<String, List<PaymentCardEntry>> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(ENTRY_SEPARATOR).mapNotNull { entry ->
            val parts = entry.split(FIELD_SEPARATOR, limit = 2)
            if (parts.size < 2) return@mapNotNull null
            val userId = escapeBack(parts[0])
            val cards = escapeBack(parts[1]).split(RECORD_SEPARATOR).filter { it.isNotBlank() }.mapNotNull { record ->
                val fields = record.split(FIELD_SEPARATOR).map(::escapeBack)
                if (fields.size < 5) return@mapNotNull null
                PaymentCardEntry(
                    id = fields[0],
                    cardholderName = fields[1],
                    cardNumberMasked = fields[2],
                    expiryDate = fields[3],
                    billingAddress = fields[4],
                )
            }
            userId to cards
        }.toMap()
    }

    private fun encodeNotificationsMap(value: Map<String, UserNotificationSettings>): String =
        value.entries.joinToString(ENTRY_SEPARATOR) { (userId, settings) ->
            listOf(
                userId,
                settings.orderUpdates.emailEnabled.toString(),
                settings.orderUpdates.pushEnabled.toString(),
                settings.promotions.emailEnabled.toString(),
                settings.promotions.pushEnabled.toString(),
                settings.newArrivals.emailEnabled.toString(),
                settings.newArrivals.pushEnabled.toString(),
                settings.wishlistAlerts.emailEnabled.toString(),
                settings.wishlistAlerts.pushEnabled.toString(),
            ).joinToString(FIELD_SEPARATOR, transform = ::escape)
        }

    private fun decodeNotificationsMap(raw: String?): Map<String, UserNotificationSettings> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(ENTRY_SEPARATOR).mapNotNull { entry ->
            val fields = entry.split(FIELD_SEPARATOR).map(::escapeBack)
            if (fields.size < 9) return@mapNotNull null
            fields[0] to UserNotificationSettings(
                orderUpdates = NotificationPreference(fields[1].toBoolean(), fields[2].toBoolean()),
                promotions = NotificationPreference(fields[3].toBoolean(), fields[4].toBoolean()),
                newArrivals = NotificationPreference(fields[5].toBoolean(), fields[6].toBoolean()),
                wishlistAlerts = NotificationPreference(fields[7].toBoolean(), fields[8].toBoolean()),
            )
        }.toMap()
    }

    private fun escape(value: String): String = value
        .replace("\\", "\\\\")
        .replace(FIELD_SEPARATOR, "\\u001F")
        .replace(ENTRY_SEPARATOR, "\\u001E")
        .replace(RECORD_SEPARATOR, "\\u001D")

    private fun escapeBack(value: String): String = value
        .replace("\\u001D", RECORD_SEPARATOR)
        .replace("\\u001E", ENTRY_SEPARATOR)
        .replace("\\u001F", FIELD_SEPARATOR)
        .replace("\\\\", "\\")

    private companion object {
        const val PREFS_NAME = "flora_account_settings"
        const val KEY_SECURITY = "security"
        const val KEY_ADDRESSES = "addresses"
        const val KEY_CARDS = "cards"
        const val KEY_NOTIFICATIONS = "notifications"
        const val FIELD_SEPARATOR = "\u001F"
        const val ENTRY_SEPARATOR = "\u001E"
        const val RECORD_SEPARATOR = "\u001D"
    }
}
