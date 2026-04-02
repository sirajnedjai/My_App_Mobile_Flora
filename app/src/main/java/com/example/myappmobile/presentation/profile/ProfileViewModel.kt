package com.example.myappmobile.presentation.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ProfileViewModel : ViewModel() {

    private val darkModeEnabled = MutableStateFlow(false)

    val uiState: StateFlow<ProfileUiState> = combine(
        AppContainer.authRepository.currentUser,
        darkModeEnabled,
    ) { user, isDarkMode ->
        ProfileUiState(
            user = user,
            darkModeEnabled = isDarkMode,
            buyerSettings = buyerSettings(),
            sellerSettings = sellerSettings(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(),
    )

    fun onDarkModeToggled(enabled: Boolean) {
        darkModeEnabled.update { enabled }
    }

    private fun buyerSettings() = listOf(
        ProfileSettingItemUi(
            id = "personal_information",
            title = "Personal Information",
            subtitle = "Update your name, email, and member details.",
            icon = Icons.Outlined.PersonOutline,
        ),
        ProfileSettingItemUi(
            id = "password_security",
            title = "Password & Security",
            subtitle = "Manage login credentials and account protection.",
            icon = Icons.Outlined.Lock,
        ),
        ProfileSettingItemUi(
            id = "saved_addresses",
            title = "Saved Addresses",
            subtitle = "Review delivery destinations and concierge notes.",
            icon = Icons.Outlined.LocationOn,
        ),
        ProfileSettingItemUi(
            id = "payment_methods",
            title = "Payment Methods",
            subtitle = "Control preferred cards and luxury checkout options.",
            icon = Icons.Outlined.CreditCard,
        ),
        ProfileSettingItemUi(
            id = "buyer_notifications",
            title = "Buyer Notifications",
            subtitle = "Choose updates for orders, releases, and private drops.",
            icon = Icons.Outlined.Notifications,
        ),
    )

    private fun sellerSettings() = listOf(
        ProfileSettingItemUi(
            id = "store_configuration",
            title = "Store Configuration",
            subtitle = "Refine storefront details, collections, and presentation.",
            icon = Icons.Outlined.Storefront,
        ),
        ProfileSettingItemUi(
            id = "payments_payouts",
            title = "Payments & Payouts",
            subtitle = "Track artisan earnings, balances, and transfers.",
            icon = Icons.Outlined.Payments,
        ),
        ProfileSettingItemUi(
            id = "shipping_logistics",
            title = "Shipping & Logistics",
            subtitle = "Manage fulfillment windows, packaging, and carriers.",
            icon = Icons.Outlined.LocalShipping,
        ),
        ProfileSettingItemUi(
            id = "seller_notifications",
            title = "Seller Notifications",
            subtitle = "Control alerts for orders, buyers, and studio activity.",
            icon = Icons.Outlined.Inventory2,
        ),
    )
}
