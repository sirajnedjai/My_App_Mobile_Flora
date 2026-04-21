package com.example.myappmobile.presentation.profile

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Storefront
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.R
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.core.localization.LanguageManager
import com.example.myappmobile.data.remote.toApiException
import com.example.myappmobile.data.repository.AccountProfilePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val sellerDashboardState = MutableStateFlow(SellerDashboardState())

    private val sellerSummaryState = combine(
        sellerDashboardState,
        AppContainer.sellerVerificationRepository.verificationState,
    ) { sellerDashboard, sellerVerification ->
        sellerDashboard to sellerVerification
    }

    val uiState: StateFlow<ProfileUiState> = combine(
        AppContainer.authRepository.currentUser,
        AppContainer.uiPreferencesRepository.isDarkMode,
        AppContainer.uiPreferencesRepository.accountProfiles,
        AppContainer.uiPreferencesRepository.languageCode,
        sellerSummaryState,
    ) { user, isDarkMode, profiles, languageCode, sellerSummary ->
        val (sellerDashboard, sellerVerification) = sellerSummary
        val profile = profiles[user.id].orEmpty()
        ProfileUiState(
            user = user.copy(
                fullName = profile.fullName.ifBlank { user.fullName },
                email = profile.email.ifBlank { user.email },
                phone = profile.phoneNumber.ifBlank { user.phone },
                avatarUrl = profile.avatarUri.ifBlank { user.avatarUrl },
            ),
            phoneNumber = profile.phoneNumber.ifBlank { user.phone },
            address = profile.address,
            darkModeEnabled = isDarkMode,
            buyerSettings = buyerSettings(),
            sellerSettings = if (user.isSeller) sellerSettings(sellerDashboard.summary) else emptyList(),
            sellerDashboardSummary = sellerDashboard.summary,
            sellerDashboardLoading = sellerDashboard.isLoading,
            sellerDashboardError = sellerDashboard.errorMessage,
            showSellerTools = user.isSeller,
            selectedLanguageCode = languageCode,
            sellerApprovalStatus = sellerVerification.status,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(),
    )

    init {
        refreshSellerDashboard()
        refreshSellerVerification()
    }

    fun onDarkModeToggled(enabled: Boolean) {
        AppContainer.uiPreferencesRepository.setDarkMode(enabled, getApplication())
    }

    fun onLanguageSelected(languageCode: String) {
        AppContainer.uiPreferencesRepository.setLanguageCode(languageCode)
        LanguageManager.applyLanguage(getApplication(), languageCode)
    }

    fun refreshSellerDashboard() {
        viewModelScope.launch {
            val user = AppContainer.authRepository.currentUser.value
            Log.d(TAG, "Refreshing seller dashboard. userId=${user.id} isSeller=${user.isSeller}")
            if (!user.isAuthenticated || !user.isSeller) {
                sellerDashboardState.value = SellerDashboardState()
                return@launch
            }

            sellerDashboardState.update { it.copy(isLoading = true, errorMessage = null) }
            AppContainer.sellerDashboardRepository.fetchSummary()
                .onSuccess { summary ->
                    sellerDashboardState.value = SellerDashboardState(
                        summary = SellerDashboardSummaryUi(
                            storeName = summary.storeName,
                            totalProducts = summary.totalProducts,
                            totalOrders = summary.totalOrders,
                            pendingOrders = summary.pendingProcessingOrders,
                            deliveredOrders = summary.deliveredOrders,
                            lowStockProducts = summary.lowStockProducts,
                            availableBalance = summary.availableBalance,
                            lifetimeEarnings = summary.lifetimeEarnings,
                            insight = summary.insights.firstOrNull().orEmpty(),
                        ),
                    )
                }
                .onFailure { error ->
                    val apiError = error.toApiException()
                    Log.d(TAG, "Seller dashboard refresh failed. error=${apiError.message}")
                    sellerDashboardState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = apiError.message,
                        )
                    }
                }
        }
    }

    fun refreshSellerVerification() {
        viewModelScope.launch {
            val user = AppContainer.authRepository.currentUser.value
            if (!user.isAuthenticated || !user.isSeller) return@launch
            AppContainer.sellerVerificationRepository.refreshVerification()
        }
    }

    private fun buyerSettings() = listOf(
        ProfileSettingItemUi(
            id = "personal_information",
            titleRes = R.string.profile_personal_information,
            subtitleRes = R.string.profile_personal_information_subtitle,
            icon = Icons.Outlined.PersonOutline,
        ),
        ProfileSettingItemUi(
            id = "application_language",
            titleRes = R.string.profile_application_language,
            subtitleRes = R.string.profile_application_language_subtitle,
            icon = Icons.Outlined.Language,
        ),
        ProfileSettingItemUi(
            id = "password_security",
            titleRes = R.string.profile_password_security,
            subtitleRes = R.string.profile_password_security_subtitle,
            icon = Icons.Outlined.Lock,
        ),
        ProfileSettingItemUi(
            id = "saved_addresses",
            titleRes = R.string.profile_saved_addresses,
            subtitleRes = R.string.profile_saved_addresses_subtitle,
            icon = Icons.Outlined.LocationOn,
        ),
        ProfileSettingItemUi(
            id = "payment_methods",
            titleRes = R.string.profile_payment_methods,
            subtitleRes = R.string.profile_payment_methods_subtitle,
            icon = Icons.Outlined.CreditCard,
        ),
        ProfileSettingItemUi(
            id = "buyer_notifications",
            titleRes = R.string.profile_buyer_notifications,
            subtitleRes = R.string.profile_buyer_notifications_subtitle,
            icon = Icons.Outlined.Notifications,
        ),
        ProfileSettingItemUi(
            id = "track_my_orders",
            titleRes = R.string.profile_track_my_orders,
            subtitleRes = R.string.profile_track_my_orders_subtitle,
            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
        ),
    )

    private fun sellerSettings(summary: SellerDashboardSummaryUi?) = listOf(
        ProfileSettingItemUi(
            id = "store_configuration",
            titleRes = R.string.profile_store_configuration,
            subtitleRes = R.string.profile_store_configuration_subtitle,
            icon = Icons.Outlined.Storefront,
            subtitleOverride = summary?.storeName
                ?.takeIf(String::isNotBlank)
                ?.let { "$it storefront and brand profile" },
        ),
        ProfileSettingItemUi(
            id = "payments_payouts",
            titleRes = R.string.profile_payments_payouts,
            subtitleRes = R.string.profile_payments_payouts_subtitle,
            icon = Icons.Outlined.Payments,
            subtitleOverride = summary?.let {
                "Balance ${formatCompactCurrency(it.availableBalance)} · Lifetime ${formatCompactCurrency(it.lifetimeEarnings)}"
            },
        ),
        ProfileSettingItemUi(
            id = "shipping_logistics",
            titleRes = R.string.profile_shipping_logistics,
            subtitleRes = R.string.profile_shipping_logistics_subtitle,
            icon = Icons.Outlined.LocalShipping,
            subtitleOverride = summary?.let { "${it.pendingOrders} pending orders await fulfillment" },
        ),
        ProfileSettingItemUi(
            id = "seller_notifications",
            titleRes = R.string.profile_seller_notifications,
            subtitleRes = R.string.profile_seller_notifications_subtitle,
            icon = Icons.Outlined.Inventory2,
            subtitleOverride = summary?.insight?.takeIf(String::isNotBlank),
        ),
        ProfileSettingItemUi(
            id = "manage_products",
            titleRes = R.string.profile_manage_products,
            subtitleRes = R.string.profile_manage_products_subtitle,
            icon = Icons.Outlined.Storefront,
            subtitleOverride = summary?.let { "${it.totalProducts} products · ${it.lowStockProducts} low stock" },
        ),
        ProfileSettingItemUi(
            id = "received_orders",
            titleRes = R.string.profile_received_orders,
            subtitleRes = R.string.profile_received_orders_subtitle,
            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
            subtitleOverride = summary?.let { "${it.totalOrders} total · ${it.deliveredOrders} delivered" },
        ),
    )

    private fun AccountProfilePreferences?.orEmpty(): AccountProfilePreferences = this ?: AccountProfilePreferences()

    private fun formatCompactCurrency(value: Double): String {
        val absolute = kotlin.math.abs(value)
        return when {
            absolute >= 1_000_000 -> "${"%.1f".format(value / 1_000_000)}M"
            absolute >= 1_000 -> "${"%.1f".format(value / 1_000)}K"
            else -> "%.0f".format(value)
        }
    }

    private data class SellerDashboardState(
        val isLoading: Boolean = false,
        val summary: SellerDashboardSummaryUi? = null,
        val errorMessage: String? = null,
    )

    private companion object {
        const val TAG = "ProfileViewModel"
    }
}
