package com.example.myappmobile.presentation.profile.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.data.repository.NotificationPreference
import com.example.myappmobile.data.repository.UserNotificationSettings
import com.example.myappmobile.domain.model.AppNotification
import com.example.myappmobile.presentation.profile.settings.SettingsCard
import com.example.myappmobile.presentation.profile.settings.SettingsHeroCard
import com.example.myappmobile.presentation.profile.settings.SettingsScaffold
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SellerNotificationToggleUi(
    val key: String,
    val title: String,
    val description: String,
    val preference: NotificationPreference,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

data class SellerNotificationsUiState(
    val isSeller: Boolean = false,
    val notifications: List<AppNotification> = emptyList(),
    val toggles: List<SellerNotificationToggleUi> = emptyList(),
)

class SellerNotificationsViewModel : ViewModel() {
    val uiState: StateFlow<SellerNotificationsUiState> = combine(
        AppContainer.authRepository.currentUser,
        AppContainer.accountSettingsRepository.notificationSettings,
        AppContainer.notificationRepository.notificationsByUser,
    ) { user, _, notificationsByUser ->
        val settings = AppContainer.accountSettingsRepository.getNotificationSettings(user.id)
        SellerNotificationsUiState(
            isSeller = user.isSeller,
            notifications = notificationsByUser[user.id].orEmpty(),
            toggles = listOf(
                SellerNotificationToggleUi(
                    key = "order_updates",
                    title = "Orders & Fulfillment",
                    description = "Receive alerts for new purchases, fulfillment changes, and delivery milestones.",
                    preference = settings.orderUpdates,
                    icon = Icons.Outlined.LocalShipping,
                ),
                SellerNotificationToggleUi(
                    key = "promotions",
                    title = "Payments & Account Notices",
                    description = "Stay informed about payouts, account updates, and operational reminders.",
                    preference = settings.promotions,
                    icon = Icons.Outlined.Payments,
                ),
                SellerNotificationToggleUi(
                    key = "new_arrivals",
                    title = "Reviews & Customer Feedback",
                    description = "Get notified when buyers leave new reviews or product feedback.",
                    preference = settings.newArrivals,
                    icon = Icons.Outlined.RateReview,
                ),
                SellerNotificationToggleUi(
                    key = "wishlist_alerts",
                    title = "Studio Activity",
                    description = "Track noteworthy activity around your listings and storefront visibility.",
                    preference = settings.wishlistAlerts,
                    icon = Icons.Outlined.Campaign,
                ),
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SellerNotificationsUiState(),
    )

    fun toggleEmail(key: String, enabled: Boolean) = updateSettings(key) { copy(emailEnabled = enabled) }

    fun togglePush(key: String, enabled: Boolean) = updateSettings(key) { copy(pushEnabled = enabled) }

    fun markAsRead(notificationId: String) {
        val userId = AppContainer.authRepository.currentUser.value.id
        AppContainer.notificationRepository.markAsRead(userId, notificationId)
    }

    fun markAllAsRead() {
        val userId = AppContainer.authRepository.currentUser.value.id
        AppContainer.notificationRepository.markAllAsRead(userId)
    }

    fun refreshNotifications() {
        val userId = AppContainer.authRepository.currentUser.value.id
        if (userId.isBlank()) return
        viewModelScope.launch {
            AppContainer.notificationBackendApi.fetchNotifications(userId)
                .getOrDefault(emptyList())
                .forEach(AppContainer.notificationRepository::saveNotification)
        }
    }

    private fun updateSettings(
        key: String,
        transform: NotificationPreference.() -> NotificationPreference,
    ) {
        val userId = AppContainer.authRepository.currentUser.value.id
        val current = AppContainer.accountSettingsRepository.getNotificationSettings(userId)
        val updated = when (key) {
            "order_updates" -> current.copy(orderUpdates = current.orderUpdates.transform())
            "promotions" -> current.copy(promotions = current.promotions.transform())
            "new_arrivals" -> current.copy(newArrivals = current.newArrivals.transform())
            "wishlist_alerts" -> current.copy(wishlistAlerts = current.wishlistAlerts.transform())
            else -> current
        }
        AppContainer.accountSettingsRepository.saveNotificationSettings(userId, updated)
    }
}

@Composable
fun SellerNotificationsScreen(
    onBack: () -> Unit,
    onOpenOrder: (String) -> Unit = {},
    viewModel: SellerNotificationsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshNotifications()
    }

    SettingsScaffold(title = "Seller Notifications", onBack = onBack) { padding ->
        if (!uiState.isSeller) {
            SellerSettingsUnavailableState(
                padding = padding,
                message = "Seller notification tools are available only for seller accounts.",
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FloraBeige)
                    .padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    SettingsHeroCard(
                        title = "Stay informed about orders, reviews, payouts, and studio activity from one seller-focused inbox.",
                        subtitle = "Notification preferences are saved per authenticated seller account. Real-time seller preference endpoints are not exposed yet, so the app persists them locally alongside your session-backed profile.",
                        icon = Icons.Outlined.NotificationsActive,
                    )
                }

                if (uiState.notifications.isEmpty()) {
                    item {
                        SettingsCard("Seller Inbox") {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Outlined.Inventory2,
                                    contentDescription = null,
                                    tint = FloraBrown,
                                )
                                Text(
                                    text = "No seller updates yet.",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FloraText,
                                )
                                Text(
                                    text = "Order activity and review-related alerts will appear here when the backend sends them.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FloraTextSecondary,
                                )
                            }
                        }
                    }
                } else {
                    item {
                        SettingsCard("Seller Inbox") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "${uiState.notifications.count { !it.isRead }} unread",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FloraTextSecondary,
                                )
                                Row(
                                    modifier = Modifier.clickable { viewModel.markAllAsRead() },
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Outlined.MarkEmailRead,
                                        contentDescription = null,
                                        tint = FloraBrown,
                                    )
                                    Text(
                                        text = "Mark all as read",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = FloraBrown,
                                    )
                                }
                            }
                        }
                    }

                    items(uiState.notifications, key = AppNotification::id) { notification ->
                        SettingsCard(notification.title) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.markAsRead(notification.id)
                                        if (notification.relatedOrderId.isNotBlank()) {
                                            onOpenOrder(notification.relatedOrderId)
                                        }
                                    },
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = if (notification.isRead) "Read" else "New",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (notification.isRead) FloraTextSecondary else FloraBrown,
                                    )
                                    Text(
                                        text = notification.createdAt,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FloraTextSecondary,
                                    )
                                }
                                Text(
                                    text = notification.body,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FloraText,
                                )
                                if (notification.relatedOrderId.isNotBlank()) {
                                    Text(
                                        text = "Open related order",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = FloraBrown,
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    SettingsCard("Notification Preferences") {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            uiState.toggles.forEach { toggle ->
                                SellerNotificationToggleRow(
                                    item = toggle,
                                    onEmailChanged = { enabled -> viewModel.toggleEmail(toggle.key, enabled) },
                                    onPushChanged = { enabled -> viewModel.togglePush(toggle.key, enabled) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SellerNotificationToggleRow(
    item: SellerNotificationToggleUi,
    onEmailChanged: (Boolean) -> Unit,
    onPushChanged: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            androidx.compose.material3.Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = FloraBrown,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = FloraText,
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FloraTextSecondary,
                )
            }
        }
        NotificationSwitchRow(
            label = "Email alerts",
            checked = item.preference.emailEnabled,
            onCheckedChange = onEmailChanged,
        )
        NotificationSwitchRow(
            label = "Push alerts",
            checked = item.preference.pushEnabled,
            onCheckedChange = onPushChanged,
        )
    }
}

@Composable
private fun NotificationSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = FloraText,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
