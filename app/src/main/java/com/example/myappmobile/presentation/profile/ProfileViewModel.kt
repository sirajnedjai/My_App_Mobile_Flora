package com.example.myappmobile.presentation.profile

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
import com.example.myappmobile.data.repository.AccountProfilePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel(
    application: Application,
) : AndroidViewModel(application) {

    val uiState: StateFlow<ProfileUiState> = combine(
        AppContainer.authRepository.currentUser,
        AppContainer.uiPreferencesRepository.isDarkMode,
        AppContainer.uiPreferencesRepository.accountProfiles,
        AppContainer.uiPreferencesRepository.languageCode,
    ) { user, isDarkMode, profiles, languageCode ->
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
            sellerSettings = if (user.isSeller) sellerSettings() else emptyList(),
            showSellerTools = user.isSeller,
            selectedLanguageCode = languageCode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(),
    )

    fun onDarkModeToggled(enabled: Boolean) {
        AppContainer.uiPreferencesRepository.setDarkMode(enabled, getApplication())
    }

    fun onLanguageSelected(languageCode: String) {
        AppContainer.uiPreferencesRepository.setLanguageCode(languageCode)
        LanguageManager.applyLanguage(getApplication(), languageCode)
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

    private fun sellerSettings() = listOf(
        ProfileSettingItemUi(
            id = "store_configuration",
            titleRes = R.string.profile_store_configuration,
            subtitleRes = R.string.profile_store_configuration_subtitle,
            icon = Icons.Outlined.Storefront,
        ),
        ProfileSettingItemUi(
            id = "payments_payouts",
            titleRes = R.string.profile_payments_payouts,
            subtitleRes = R.string.profile_payments_payouts_subtitle,
            icon = Icons.Outlined.Payments,
        ),
        ProfileSettingItemUi(
            id = "shipping_logistics",
            titleRes = R.string.profile_shipping_logistics,
            subtitleRes = R.string.profile_shipping_logistics_subtitle,
            icon = Icons.Outlined.LocalShipping,
        ),
        ProfileSettingItemUi(
            id = "seller_notifications",
            titleRes = R.string.profile_seller_notifications,
            subtitleRes = R.string.profile_seller_notifications_subtitle,
            icon = Icons.Outlined.Inventory2,
        ),
        ProfileSettingItemUi(
            id = "manage_products",
            titleRes = R.string.profile_manage_products,
            subtitleRes = R.string.profile_manage_products_subtitle,
            icon = Icons.Outlined.Storefront,
        ),
        ProfileSettingItemUi(
            id = "received_orders",
            titleRes = R.string.profile_received_orders,
            subtitleRes = R.string.profile_received_orders_subtitle,
            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
        ),
    )

    private fun AccountProfilePreferences?.orEmpty(): AccountProfilePreferences = this ?: AccountProfilePreferences()
}
