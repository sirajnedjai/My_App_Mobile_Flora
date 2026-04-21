package com.example.myappmobile.presentation.profile.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.StoreMallDirectory
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.components.AuthTextField
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.data.repository.SellerShippingPreferences
import com.example.myappmobile.domain.model.User
import com.example.myappmobile.presentation.profile.settings.SettingsCard
import com.example.myappmobile.presentation.profile.settings.SettingsHeroCard
import com.example.myappmobile.presentation.profile.settings.SettingsScaffold
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class SellerShippingLogisticsUiState(
    val isSeller: Boolean = false,
    val standardDeliveryEnabled: Boolean = true,
    val expressDeliveryEnabled: Boolean = true,
    val localPickupEnabled: Boolean = false,
    val dispatchWindow: String = "",
    val fulfillmentNotes: String = "",
    val successMessage: String? = null,
)

class SellerShippingLogisticsViewModel : ViewModel() {
    private val editor = MutableStateFlow(SellerShippingLogisticsUiState())

    val uiState: StateFlow<SellerShippingLogisticsUiState> = combine(
        AppContainer.authRepository.currentUser,
        AppContainer.uiPreferencesRepository.sellerShippingPreferences,
        editor,
    ) { user, _, state ->
        val preferences = sellerPreferences(user)
        state.copy(
            isSeller = user.isSeller,
            standardDeliveryEnabled = preferences.standardDeliveryEnabled,
            expressDeliveryEnabled = preferences.expressDeliveryEnabled,
            localPickupEnabled = preferences.localPickupEnabled,
            dispatchWindow = state.dispatchWindow.ifBlank { preferences.dispatchWindow },
            fulfillmentNotes = state.fulfillmentNotes.ifBlank { preferences.fulfillmentNotes },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SellerShippingLogisticsUiState(),
    )

    fun onStandardDeliveryChanged(enabled: Boolean) = editor.update {
        it.copy(standardDeliveryEnabled = enabled, successMessage = null)
    }

    fun onExpressDeliveryChanged(enabled: Boolean) = editor.update {
        it.copy(expressDeliveryEnabled = enabled, successMessage = null)
    }

    fun onLocalPickupChanged(enabled: Boolean) = editor.update {
        it.copy(localPickupEnabled = enabled, successMessage = null)
    }

    fun onDispatchWindowChanged(value: String) = editor.update {
        it.copy(dispatchWindow = value, successMessage = null)
    }

    fun onFulfillmentNotesChanged(value: String) = editor.update {
        it.copy(fulfillmentNotes = value, successMessage = null)
    }

    fun save() {
        val user = AppContainer.authRepository.currentUser.value
        if (!user.isSeller) return
        AppContainer.uiPreferencesRepository.saveSellerShippingPreferences(
            sellerId = user.id,
            preferences = SellerShippingPreferences(
                standardDeliveryEnabled = uiState.value.standardDeliveryEnabled,
                expressDeliveryEnabled = uiState.value.expressDeliveryEnabled,
                localPickupEnabled = uiState.value.localPickupEnabled,
                dispatchWindow = uiState.value.dispatchWindow.trim().ifBlank { "Ships within 2 business days" },
                fulfillmentNotes = uiState.value.fulfillmentNotes.trim(),
            ),
        )
        editor.update { it.copy(successMessage = "Shipping preferences updated for your FLORA storefront.") }
    }

    private fun sellerPreferences(user: User): SellerShippingPreferences =
        if (user.isSeller) {
            AppContainer.uiPreferencesRepository.getSellerShippingPreferences(user.id)
        } else {
            SellerShippingPreferences()
        }
}

@Composable
fun SellerShippingLogisticsScreen(
    onBack: () -> Unit = {},
    viewModel: SellerShippingLogisticsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScaffold(title = "Shipping & Logistics", onBack = onBack) { padding ->
        if (!uiState.isSeller) {
            SellerSettingsUnavailableState(
                padding = padding,
                message = "Shipping tools are available only for seller accounts.",
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
                        title = "Configure the fulfillment options buyers see when ordering from your FLORA store.",
                        subtitle = "These preferences are stored with the seller profile data currently available in the app. Backend shipping-policy endpoints are not exposed yet.",
                        icon = Icons.Outlined.LocalShipping,
                    )
                }
                item {
                    SettingsCard("Delivery Methods") {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            ShippingToggleRow(
                                title = "Standard delivery",
                                description = "Offer your default delivery service for routine nationwide orders.",
                                checked = uiState.standardDeliveryEnabled,
                                onCheckedChange = viewModel::onStandardDeliveryChanged,
                            )
                            ShippingToggleRow(
                                title = "Express delivery",
                                description = "Enable priority shipping for time-sensitive purchases when available.",
                                checked = uiState.expressDeliveryEnabled,
                                onCheckedChange = viewModel::onExpressDeliveryChanged,
                            )
                            ShippingToggleRow(
                                title = "Studio pickup",
                                description = "Allow buyers to collect finished pieces directly from your atelier.",
                                checked = uiState.localPickupEnabled,
                                onCheckedChange = viewModel::onLocalPickupChanged,
                            )
                        }
                    }
                }
                item {
                    SettingsCard("Fulfillment Preferences") {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            AuthTextField(
                                label = "Dispatch window",
                                value = uiState.dispatchWindow,
                                onValueChange = viewModel::onDispatchWindowChanged,
                                placeholder = "Ships within 2 business days",
                            )
                            AuthTextField(
                                label = "Seller notes",
                                value = uiState.fulfillmentNotes,
                                onValueChange = viewModel::onFulfillmentNotesChanged,
                                placeholder = "Share packaging details, carrier guidance, or pickup instructions.",
                            )
                            uiState.successMessage?.let { message ->
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FloraBrown,
                                )
                            }
                            PrimaryButton(
                                text = "Save Shipping Preferences",
                                onClick = viewModel::save,
                            )
                        }
                    }
                }
                item {
                    SettingsCard("Operational Guidance") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            LogisticsInfoRow(
                                icon = Icons.Outlined.Inventory2,
                                title = "Availability stays separate",
                                description = "Inventory and listing availability continue to be managed from Manage Products.",
                            )
                            LogisticsInfoRow(
                                icon = Icons.Outlined.StoreMallDirectory,
                                title = "Store identity is reused",
                                description = "Pickup and dispatch details complement the storefront configuration already saved for your seller account.",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShippingToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = FloraText)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = FloraTextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun LogisticsInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FloraBrown,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = FloraText)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = FloraTextSecondary)
        }
    }
}

@Composable
internal fun SellerSettingsUnavailableState(
    padding: PaddingValues,
    message: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FloraBeige)
            .padding(padding)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = FloraTextSecondary,
        )
    }
}
