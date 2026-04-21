package com.example.myappmobile.data.repository

import android.content.Context
import com.example.myappmobile.domain.model.SellerApprovalStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AccountProfilePreferences(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val avatarUri: String = "",
)

data class StoreConfigurationPreferences(
    val shopName: String = "",
    val establishmentDate: String = "",
    val ownerName: String = "",
    val logoUri: String = "",
    val description: String = "",
)

data class SellerWalletPreferences(
    val availableBalance: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val totalWithdrawn: Double = 0.0,
    val transferRecords: List<SellerTransferRecordPreferences> = emptyList(),
)

data class SellerTransferRecordPreferences(
    val id: String,
    val date: String,
    val type: String,
    val status: String,
    val amountSent: Double,
    val orderNumber: String,
    val paymentMethod: String = "",
)

data class SellerShippingPreferences(
    val standardDeliveryEnabled: Boolean = true,
    val expressDeliveryEnabled: Boolean = true,
    val localPickupEnabled: Boolean = false,
    val dispatchWindow: String = "Ships within 2 business days",
    val fulfillmentNotes: String = "",
)

class UiPreferencesRepository {
    private var appContext: Context? = null

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _accountProfiles = MutableStateFlow<Map<String, AccountProfilePreferences>>(emptyMap())
    val accountProfiles: StateFlow<Map<String, AccountProfilePreferences>> = _accountProfiles.asStateFlow()

    private val _storeConfigurations = MutableStateFlow<Map<String, StoreConfigurationPreferences>>(emptyMap())
    val storeConfigurations: StateFlow<Map<String, StoreConfigurationPreferences>> = _storeConfigurations.asStateFlow()

    private val _sellerWallets = MutableStateFlow<Map<String, SellerWalletPreferences>>(emptyMap())
    val sellerWallets: StateFlow<Map<String, SellerWalletPreferences>> = _sellerWallets.asStateFlow()

    private val _sellerShippingPreferences = MutableStateFlow<Map<String, SellerShippingPreferences>>(emptyMap())
    val sellerShippingPreferences: StateFlow<Map<String, SellerShippingPreferences>> = _sellerShippingPreferences.asStateFlow()

    private val _sellerApprovalStatuses = MutableStateFlow<Map<String, SellerApprovalStatus>>(emptyMap())
    val sellerApprovalStatuses: StateFlow<Map<String, SellerApprovalStatus>> = _sellerApprovalStatuses.asStateFlow()

    private val _languageCode = MutableStateFlow("en")
    val languageCode: StateFlow<String> = _languageCode.asStateFlow()

    fun initialize(context: Context) {
        appContext = context.applicationContext
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isDarkMode.value = prefs.getBoolean(KEY_DARK_MODE, false)
        _languageCode.value = prefs.getString(KEY_LANGUAGE_CODE, "en").orEmpty().ifBlank { "en" }
        _accountProfiles.value = decodeAccountProfiles(prefs.getString(KEY_ACCOUNT_PROFILES, null))
        _storeConfigurations.value = decodeStoreConfigurations(prefs.getString(KEY_STORE_CONFIGS, null))
        _sellerWallets.value = decodeSellerWallets(prefs.getString(KEY_SELLER_WALLETS, null))
        _sellerShippingPreferences.value = decodeSellerShippingPreferences(prefs.getString(KEY_SELLER_SHIPPING, null))
        _sellerApprovalStatuses.value = decodeSellerApprovalStatuses(prefs.getString(KEY_SELLER_APPROVAL_STATUSES, null))
    }

    fun setDarkMode(enabled: Boolean, context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_MODE, enabled)
            .apply()
        _isDarkMode.value = enabled
    }

    fun setLanguageCode(languageCode: String) {
        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE_CODE, languageCode)
            .apply()
        _languageCode.value = languageCode
    }

    fun getAccountProfile(userId: String): AccountProfilePreferences =
        _accountProfiles.value[userId]
            ?: _accountProfiles.value[normalizeSellerStoreId(userId)]
            ?: _accountProfiles.value[denormalizeSellerStoreId(userId)]
            ?: AccountProfilePreferences()

    fun saveAccountProfile(userId: String, profile: AccountProfilePreferences) {
        val updated = _accountProfiles.value + (userId to profile)
        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ACCOUNT_PROFILES, encodeAccountProfiles(updated))
            .apply()
        _accountProfiles.value = updated
    }

    fun getStoreConfiguration(sellerId: String): StoreConfigurationPreferences =
        _storeConfigurations.value[normalizeSellerStoreId(sellerId)].orEmpty()

    fun saveStoreConfiguration(sellerId: String, configuration: StoreConfigurationPreferences) {
        val normalizedSellerId = normalizeSellerStoreId(sellerId)
        val updated = _storeConfigurations.value + (normalizedSellerId to configuration)
        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_STORE_CONFIGS, encodeStoreConfigurations(updated))
            .apply()
        _storeConfigurations.value = updated
    }

    fun getSellerWallet(sellerId: String): SellerWalletPreferences =
        _sellerWallets.value[normalizeSellerStoreId(sellerId)] ?: defaultSellerWallet(normalizeSellerStoreId(sellerId))

    fun saveSellerWallet(sellerId: String, wallet: SellerWalletPreferences) {
        val normalizedSellerId = normalizeSellerStoreId(sellerId)
        val updated = _sellerWallets.value + (normalizedSellerId to wallet)
        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SELLER_WALLETS, encodeSellerWallets(updated))
            .apply()
        _sellerWallets.value = updated
    }

    fun getSellerShippingPreferences(sellerId: String): SellerShippingPreferences =
        _sellerShippingPreferences.value[normalizeSellerStoreId(sellerId)] ?: SellerShippingPreferences()

    fun saveSellerShippingPreferences(sellerId: String, preferences: SellerShippingPreferences) {
        val normalizedSellerId = normalizeSellerStoreId(sellerId)
        val updated = _sellerShippingPreferences.value + (normalizedSellerId to preferences)
        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SELLER_SHIPPING, encodeSellerShippingPreferences(updated))
            .apply()
        _sellerShippingPreferences.value = updated
    }

    fun getSellerApprovalStatus(sellerId: String): SellerApprovalStatus =
        _sellerApprovalStatuses.value[normalizeSellerStoreId(sellerId)]
            ?: defaultSellerApprovalStatus(sellerId)

    fun findSellerApprovalStatus(sellerId: String): SellerApprovalStatus? =
        _sellerApprovalStatuses.value[normalizeSellerStoreId(sellerId)]

    fun saveSellerApprovalStatus(sellerId: String, status: SellerApprovalStatus) {
        val normalizedSellerId = normalizeSellerStoreId(sellerId)
        val updated = _sellerApprovalStatuses.value + (normalizedSellerId to status)
        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SELLER_APPROVAL_STATUSES, encodeSellerApprovalStatuses(updated))
            .apply()
        _sellerApprovalStatuses.value = updated
    }

    fun processSellerWithdrawal(
        sellerId: String,
        amount: Double,
        paymentMethod: String,
    ): SellerWalletPreferences {
        val wallet = getSellerWallet(sellerId)
        val updatedWallet = wallet.copy(
            availableBalance = (wallet.availableBalance - amount).coerceAtLeast(0.0),
            totalWithdrawn = wallet.totalWithdrawn + amount,
            transferRecords = buildList {
                add(
                    SellerTransferRecordPreferences(
                        id = "txn_${System.currentTimeMillis()}",
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                        type = "Withdrawal",
                        status = "Completed",
                        amountSent = amount,
                        orderNumber = "WD-${(10000..99999).random()}",
                        paymentMethod = paymentMethod,
                    ),
                )
                addAll(wallet.transferRecords)
            },
        )
        saveSellerWallet(sellerId, updatedWallet)
        return updatedWallet
    }

    fun resolveSellerAvatar(sellerId: String, fallback: String = ""): String {
        val normalizedSellerId = normalizeSellerStoreId(sellerId)
        val accountAvatar = getAccountProfile(normalizedSellerId).avatarUri
        val storeAvatar = getStoreConfiguration(normalizedSellerId).logoUri
        return accountAvatar.ifBlank { storeAvatar.ifBlank { fallback } }
    }

    fun normalizeSellerStoreId(sellerId: String): String = when (sellerId) {
        "s1" -> "1"
        else -> sellerId
    }

    private fun denormalizeSellerStoreId(sellerId: String): String = when (sellerId) {
        "1" -> "s1"
        else -> sellerId
    }

    private fun requireContext(): Context = checkNotNull(appContext) {
        "UiPreferencesRepository is not initialized. Call initialize(context) first."
    }

    private fun StoreConfigurationPreferences?.orEmpty(): StoreConfigurationPreferences = this ?: StoreConfigurationPreferences()

    private fun encodeAccountProfiles(value: Map<String, AccountProfilePreferences>): String =
        value.entries.joinToString(ENTRY_SEPARATOR) { (userId, profile) ->
            listOf(
                userId,
                profile.fullName,
                profile.email,
                profile.phoneNumber,
                profile.address,
                profile.avatarUri,
            ).joinToString(FIELD_SEPARATOR) { encodeField(it) }
        }

    private fun decodeAccountProfiles(raw: String?): Map<String, AccountProfilePreferences> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(ENTRY_SEPARATOR).mapNotNull { entry ->
            val fields = entry.split(FIELD_SEPARATOR)
            if (fields.size < 6) return@mapNotNull null
            decodeField(fields[0]) to AccountProfilePreferences(
                fullName = decodeField(fields[1]),
                email = decodeField(fields[2]),
                phoneNumber = decodeField(fields[3]),
                address = decodeField(fields[4]),
                avatarUri = decodeField(fields[5]),
            )
        }.toMap()
    }

    private fun encodeStoreConfigurations(value: Map<String, StoreConfigurationPreferences>): String =
        value.entries.joinToString(ENTRY_SEPARATOR) { (sellerId, config) ->
            listOf(
                sellerId,
                config.shopName,
                config.establishmentDate,
                config.ownerName,
                config.logoUri,
                config.description,
            ).joinToString(FIELD_SEPARATOR) { encodeField(it) }
        }

    private fun decodeStoreConfigurations(raw: String?): Map<String, StoreConfigurationPreferences> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(ENTRY_SEPARATOR).mapNotNull { entry ->
            val fields = entry.split(FIELD_SEPARATOR)
            if (fields.size < 6) return@mapNotNull null
            decodeField(fields[0]) to StoreConfigurationPreferences(
                shopName = decodeField(fields[1]),
                establishmentDate = decodeField(fields[2]),
                ownerName = decodeField(fields[3]),
                logoUri = decodeField(fields[4]),
                description = decodeField(fields[5]),
            )
        }.toMap()
    }

    private fun encodeSellerWallets(value: Map<String, SellerWalletPreferences>): String =
        value.entries.joinToString(ENTRY_SEPARATOR) { (sellerId, wallet) ->
            listOf(
                sellerId,
                wallet.availableBalance.toString(),
                wallet.pendingBalance.toString(),
                wallet.totalWithdrawn.toString(),
                encodeTransferRecords(wallet.transferRecords),
            ).joinToString(FIELD_SEPARATOR) { encodeField(it) }
        }

    private fun decodeSellerWallets(raw: String?): Map<String, SellerWalletPreferences> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(ENTRY_SEPARATOR).mapNotNull { entry ->
            val fields = entry.split(FIELD_SEPARATOR)
            if (fields.size < 5) return@mapNotNull null
            decodeField(fields[0]) to SellerWalletPreferences(
                availableBalance = decodeField(fields[1]).toDoubleOrNull() ?: 0.0,
                pendingBalance = decodeField(fields[2]).toDoubleOrNull() ?: 0.0,
                totalWithdrawn = decodeField(fields[3]).toDoubleOrNull() ?: 0.0,
                transferRecords = decodeTransferRecords(decodeField(fields[4])),
            )
        }.toMap()
    }

    private fun encodeSellerShippingPreferences(value: Map<String, SellerShippingPreferences>): String =
        value.entries.joinToString(ENTRY_SEPARATOR) { (sellerId, preferences) ->
            listOf(
                sellerId,
                preferences.standardDeliveryEnabled.toString(),
                preferences.expressDeliveryEnabled.toString(),
                preferences.localPickupEnabled.toString(),
                preferences.dispatchWindow,
                preferences.fulfillmentNotes,
            ).joinToString(FIELD_SEPARATOR) { encodeField(it) }
        }

    private fun decodeSellerShippingPreferences(raw: String?): Map<String, SellerShippingPreferences> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(ENTRY_SEPARATOR).mapNotNull { entry ->
            val fields = entry.split(FIELD_SEPARATOR)
            if (fields.size < 6) return@mapNotNull null
            decodeField(fields[0]) to SellerShippingPreferences(
                standardDeliveryEnabled = decodeField(fields[1]).toBoolean(),
                expressDeliveryEnabled = decodeField(fields[2]).toBoolean(),
                localPickupEnabled = decodeField(fields[3]).toBoolean(),
                dispatchWindow = decodeField(fields[4]),
                fulfillmentNotes = decodeField(fields[5]),
            )
        }.toMap()
    }

    private fun encodeSellerApprovalStatuses(value: Map<String, SellerApprovalStatus>): String =
        value.entries.joinToString(ENTRY_SEPARATOR) { (sellerId, status) ->
            listOf(
                sellerId,
                status.name,
            ).joinToString(FIELD_SEPARATOR) { encodeField(it) }
        }

    private fun decodeSellerApprovalStatuses(raw: String?): Map<String, SellerApprovalStatus> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(ENTRY_SEPARATOR).mapNotNull { entry ->
            val fields = entry.split(FIELD_SEPARATOR)
            if (fields.size < 2) return@mapNotNull null
            val status = runCatching {
                SellerApprovalStatus.valueOf(decodeField(fields[1]))
            }.getOrNull() ?: return@mapNotNull null
            decodeField(fields[0]) to status
        }.toMap()
    }

    private fun encodeTransferRecords(value: List<SellerTransferRecordPreferences>): String =
        value.joinToString(RECORD_SEPARATOR) { record ->
            listOf(
                record.id,
                record.date,
                record.type,
                record.status,
                record.amountSent.toString(),
                record.orderNumber,
                record.paymentMethod,
            ).joinToString(RECORD_FIELD_SEPARATOR) { encodeField(it) }
        }

    private fun decodeTransferRecords(raw: String): List<SellerTransferRecordPreferences> {
        if (raw.isBlank()) return emptyList()
        return raw.split(RECORD_SEPARATOR).mapNotNull { record ->
            val fields = record.split(RECORD_FIELD_SEPARATOR)
            if (fields.size < 6) return@mapNotNull null
            SellerTransferRecordPreferences(
                id = decodeField(fields[0]),
                date = decodeField(fields[1]),
                type = decodeField(fields[2]),
                status = decodeField(fields[3]),
                amountSent = decodeField(fields[4]).toDoubleOrNull() ?: 0.0,
                orderNumber = decodeField(fields[5]),
                paymentMethod = fields.getOrNull(6)?.let(::decodeField).orEmpty(),
            )
        }
    }

    private fun defaultSellerWallet(sellerId: String): SellerWalletPreferences = when (normalizeSellerStoreId(sellerId)) {
        "1" -> SellerWalletPreferences(
            availableBalance = 4280.45,
            pendingBalance = 638.20,
            totalWithdrawn = 12480.00,
            transferRecords = listOf(
                SellerTransferRecordPreferences("payout_1", "2026-03-31", "Weekly Payout", "Completed", 420.50, "ORD-48321"),
                SellerTransferRecordPreferences("payout_2", "2026-03-24", "Weekly Payout", "Completed", 388.00, "ORD-48112"),
                SellerTransferRecordPreferences("payout_3", "2026-03-17", "Refund Adjustment", "Processing", 112.30, "ORD-47998"),
                SellerTransferRecordPreferences("payout_4", "2026-03-10", "Monthly Bonus", "Completed", 680.00, "ORD-47210"),
                SellerTransferRecordPreferences("payout_5", "2026-03-03", "Weekly Payout", "Completed", 401.75, "ORD-47004"),
            ),
        )
        else -> SellerWalletPreferences()
    }

    private fun defaultSellerApprovalStatus(sellerId: String): SellerApprovalStatus = when (normalizeSellerStoreId(sellerId)) {
        else -> SellerApprovalStatus.UNKNOWN
    }

    private fun encodeField(value: String): String = value
        .replace("\\", "\\\\")
        .replace(RECORD_FIELD_SEPARATOR, ESCAPED_RECORD_FIELD_SEPARATOR)
        .replace(RECORD_SEPARATOR, ESCAPED_RECORD_SEPARATOR)
        .replace(FIELD_SEPARATOR, ESCAPED_FIELD_SEPARATOR)
        .replace(ENTRY_SEPARATOR, ESCAPED_ENTRY_SEPARATOR)

    private fun decodeField(value: String): String = value
        .replace(ESCAPED_RECORD_FIELD_SEPARATOR, RECORD_FIELD_SEPARATOR)
        .replace(ESCAPED_RECORD_SEPARATOR, RECORD_SEPARATOR)
        .replace(ESCAPED_FIELD_SEPARATOR, FIELD_SEPARATOR)
        .replace(ESCAPED_ENTRY_SEPARATOR, ENTRY_SEPARATOR)
        .replace("\\\\", "\\")

    private companion object {
        const val PREFS_NAME = "flora_ui_prefs"
        const val KEY_DARK_MODE = "dark_mode_enabled"
        const val KEY_LANGUAGE_CODE = "language_code"
        const val KEY_ACCOUNT_PROFILES = "account_profiles"
        const val KEY_STORE_CONFIGS = "store_configurations"
        const val KEY_SELLER_WALLETS = "seller_wallets"
        const val KEY_SELLER_SHIPPING = "seller_shipping"
        const val KEY_SELLER_APPROVAL_STATUSES = "seller_approval_statuses"
        const val FIELD_SEPARATOR = "\u001F"
        const val ENTRY_SEPARATOR = "\u001E"
        const val RECORD_SEPARATOR = "\u001D"
        const val RECORD_FIELD_SEPARATOR = "\u001C"
        const val ESCAPED_RECORD_SEPARATOR = "\\u001D"
        const val ESCAPED_RECORD_FIELD_SEPARATOR = "\\u001C"
        const val ESCAPED_FIELD_SEPARATOR = "\\u001F"
        const val ESCAPED_ENTRY_SEPARATOR = "\\u001E"
    }
}
