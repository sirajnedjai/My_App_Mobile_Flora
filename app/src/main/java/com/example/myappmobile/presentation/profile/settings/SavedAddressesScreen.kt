package com.example.myappmobile.presentation.profile.settings

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.MaterialTheme
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
import com.example.myappmobile.core.components.OutlineButton
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.data.repository.SavedAddressEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class SavedAddressesUiState(
    val addresses: List<SavedAddressEntry> = emptyList(),
    val fullName: String = "",
    val phoneNumber: String = "",
    val state: String = "",
    val city: String = "",
    val postalCode: String = "",
    val streetAddress: String = "",
    val label: String = "Home",
    val errorMessage: String? = null,
)

class SavedAddressesViewModel : ViewModel() {
    private val editor = MutableStateFlow(SavedAddressesUiState())

    val uiState: StateFlow<SavedAddressesUiState> = combine(
        AppContainer.authRepository.currentUser,
        AppContainer.accountSettingsRepository.savedAddresses,
        editor,
    ) { user, _, state ->
        state.copy(addresses = AppContainer.accountSettingsRepository.getSavedAddresses(user.id))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SavedAddressesUiState(),
    )

    fun onFullNameChanged(value: String) = editor.update { it.copy(fullName = value, errorMessage = null) }
    fun onPhoneChanged(value: String) = editor.update { it.copy(phoneNumber = value, errorMessage = null) }
    fun onStateChanged(value: String) = editor.update { it.copy(state = value, errorMessage = null) }
    fun onCityChanged(value: String) = editor.update { it.copy(city = value, errorMessage = null) }
    fun onPostalChanged(value: String) = editor.update { it.copy(postalCode = value, errorMessage = null) }
    fun onStreetChanged(value: String) = editor.update { it.copy(streetAddress = value, errorMessage = null) }
    fun onLabelChanged(value: String) = editor.update { it.copy(label = value, errorMessage = null) }

    fun saveAddress() {
        val state = uiState.value
        val validationError = when {
            state.fullName.isBlank() -> "Full name is required."
            state.phoneNumber.isBlank() -> "Phone number is required."
            state.state.isBlank() -> "State / Wilaya is required."
            state.city.isBlank() -> "City is required."
            state.postalCode.isBlank() -> "Postal code is required."
            state.streetAddress.isBlank() -> "Street address is required."
            else -> null
        }
        if (validationError != null) {
            editor.update { it.copy(errorMessage = validationError) }
            return
        }
        val userId = AppContainer.authRepository.currentUser.value.id
        AppContainer.accountSettingsRepository.saveAddress(
            userId = userId,
            entry = SavedAddressEntry(
                id = "address_${System.currentTimeMillis()}",
                fullName = state.fullName,
                phoneNumber = state.phoneNumber,
                state = state.state,
                city = state.city,
                postalCode = state.postalCode,
                streetAddress = state.streetAddress,
                label = state.label,
                isDefault = state.addresses.isEmpty(),
            ),
        )
        editor.value = SavedAddressesUiState(addresses = AppContainer.accountSettingsRepository.getSavedAddresses(userId))
    }

    fun setDefault(addressId: String) {
        AppContainer.accountSettingsRepository.setDefaultAddress(AppContainer.authRepository.currentUser.value.id, addressId)
    }
}

@Composable
fun SavedAddressesScreen(
    onBack: () -> Unit,
    viewModel: SavedAddressesViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScaffold(title = "Saved Address", onBack = onBack) { padding ->
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
                    title = "Keep delivery details ready for a faster premium checkout experience.",
                    subtitle = "Add refined shipping addresses for home, work, or gifting.",
                    icon = Icons.Outlined.LocationOn,
                )
            }
            item {
                SettingsCard("Add Address") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AuthTextField("Full Name", uiState.fullName, viewModel::onFullNameChanged, placeholder = "Enter full name")
                        AuthTextField("Phone Number", uiState.phoneNumber, viewModel::onPhoneChanged, placeholder = "Enter phone number")
                        AuthTextField("State / Wilaya", uiState.state, viewModel::onStateChanged, placeholder = "Enter state / wilaya")
                        AuthTextField("City", uiState.city, viewModel::onCityChanged, placeholder = "Enter city")
                        AuthTextField("Postal Code", uiState.postalCode, viewModel::onPostalChanged, placeholder = "Enter postal code")
                        AuthTextField("Street Address", uiState.streetAddress, viewModel::onStreetChanged, placeholder = "Enter street address")
                        AuthTextField("Label (Home / Work)", uiState.label, viewModel::onLabelChanged, placeholder = "Home")
                        uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                        PrimaryButton(text = "Add Address", onClick = viewModel::saveAddress)
                    }
                }
            }
            if (uiState.addresses.isEmpty()) {
                item {
                    SettingsCard("Address Book") {
                        Text("No saved addresses yet.", style = MaterialTheme.typography.bodyMedium, color = FloraTextSecondary)
                    }
                }
            } else {
                items(uiState.addresses, key = SavedAddressEntry::id) { address ->
                    SettingsCard(address.label) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(address.fullName, style = MaterialTheme.typography.titleMedium, color = FloraText)
                            Text(address.phoneNumber, style = MaterialTheme.typography.bodyMedium, color = FloraTextSecondary)
                            Text("${address.streetAddress}, ${address.city}, ${address.state}", style = MaterialTheme.typography.bodyMedium, color = FloraTextSecondary)
                            Text(address.postalCode, style = MaterialTheme.typography.bodySmall, color = FloraTextSecondary)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    if (address.isDefault) "Default address" else "Available for delivery",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = FloraText,
                                )
                                if (!address.isDefault) {
                                    OutlineButton(
                                        text = "Set Default",
                                        onClick = { viewModel.setDefault(address.id) },
                                        fillMaxWidth = false,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
