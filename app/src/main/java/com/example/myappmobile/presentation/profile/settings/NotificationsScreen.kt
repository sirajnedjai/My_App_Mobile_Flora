package com.example.myappmobile.presentation.profile.settings

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
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotificationToggleUi(
    val key: String,
    val title: String,
    val description: String,
    val preference: NotificationPreference,
)

data class NotificationsUiState(
    val notifications: List<AppNotification> = emptyList(),
    val toggles: List<NotificationToggleUi> = emptyList(),
)

class NotificationsViewModel : ViewModel() {
    val uiState: StateFlow<NotificationsUiState> = combine(
        AppContainer.authRepository.currentUser,
        AppContainer.accountSettingsRepository.notificationSettings,
        AppContainer.notificationRepository.notificationsByUser,
    ) { user, _, notificationsByUser ->
        val settings = AppContainer.accountSettingsRepository.getNotificationSettings(user.id)
        NotificationsUiState(
            notifications = notificationsByUser[user.id].orEmpty(),
            toggles = listOf(
                NotificationToggleUi(
                    "order_updates",
                    "Order Updates",
                    "Stay informed about payment confirmation, shipment progress, and delivery milestones.",
                    settings.orderUpdates,
                ),
                NotificationToggleUi(
                    "promotions",
                    "Promotions",
                    "Receive curated seasonal offers and private promotions from FLORA.",
                    settings.promotions,
                ),
                NotificationToggleUi(
                    "new_arrivals",
                    "New Arrivals",
                    "Be the first to discover newly launched handmade pieces.",
                    settings.newArrivals,
                ),
                NotificationToggleUi(
                    "wishlist_alerts",
                    "Wishlist Alerts",
                    "Get notified when saved pieces return, drop in price, or sell quickly.",
                    settings.wishlistAlerts,
                ),
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NotificationsUiState(),
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

    private fun updateSettings(key: String, transform: NotificationPreference.() -> NotificationPreference) {
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
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenOrder: (String) -> Unit = {},
    viewModel: NotificationsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshNotifications()
    }

    SettingsScaffold(title = "Notifications", onBack = onBack) { padding ->
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
                    title = "Follow delivery updates and stay informed about everything happening on FLORA.",
                    subtitle = "Delivered-order notifications appear here and can also arrive as phone notifications when push is enabled.",
                    icon = Icons.Outlined.Notifications,
                )
            }

            if (uiState.notifications.isEmpty()) {
                item {
                    SettingsCard("Inbox") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Outlined.NotificationsNone,
                                contentDescription = null,
                                tint = FloraBrown,
                            )
                            Text("No notifications yet.", style = MaterialTheme.typography.titleMedium, color = FloraText)
                            Text(
                                "Delivered orders and important account updates will appear here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FloraTextSecondary,
                            )
                        }
                    }
                }
            } else {
                item {
                    SettingsCard("Inbox") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "${uiState.notifications.count { !it.isRead }} unread",
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
                                    Text("Mark all as read", style = MaterialTheme.typography.labelLarge, color = FloraBrown)
                                }
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
                            Text(notification.body, style = MaterialTheme.typography.bodyMedium, color = FloraText)
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
                        Text(
                            "Choose which updates should also arrive by email or phone notification.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FloraTextSecondary,
                        )
                    }
                }
            }

            items(uiState.toggles, key = NotificationToggleUi::key) { item ->
                SettingsCard(item.title) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(item.description, style = MaterialTheme.typography.bodyMedium, color = FloraTextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Email", style = MaterialTheme.typography.titleMedium, color = FloraText)
                            Switch(
                                checked = item.preference.emailEnabled,
                                onCheckedChange = { viewModel.toggleEmail(item.key, it) },
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Push", style = MaterialTheme.typography.titleMedium, color = FloraText)
                            Switch(
                                checked = item.preference.pushEnabled,
                                onCheckedChange = { viewModel.togglePush(item.key, it) },
                            )
                        }
                    }
                }
            }
        }
    }
}
