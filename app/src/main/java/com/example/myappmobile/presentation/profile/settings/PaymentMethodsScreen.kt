package com.example.myappmobile.presentation.profile.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.components.AuthTextField
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.utils.Validators
import com.example.myappmobile.data.repository.PaymentCardEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class PaymentMethodsUiState(
    val cards: List<PaymentCardEntry> = emptyList(),
    val cardholderName: String = "",
    val cardNumber: String = "",
    val expiryDate: String = "",
    val billingAddress: String = "",
    val errorMessage: String? = null,
)

class PaymentMethodsViewModel : ViewModel() {
    private val editor = MutableStateFlow(PaymentMethodsUiState())

    val uiState: StateFlow<PaymentMethodsUiState> = combine(
        AppContainer.authRepository.currentUser,
        AppContainer.accountSettingsRepository.paymentMethods,
        editor,
    ) { user, _, state ->
        state.copy(cards = AppContainer.accountSettingsRepository.getPaymentMethods(user.id))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PaymentMethodsUiState(),
    )

    fun onCardholderChanged(value: String) = editor.update { it.copy(cardholderName = value, errorMessage = null) }
    fun onCardNumberChanged(value: String) = editor.update { it.copy(cardNumber = value.filter(Char::isDigit).take(16), errorMessage = null) }
    fun onExpiryChanged(value: String) = editor.update { it.copy(expiryDate = value.take(5), errorMessage = null) }
    fun onBillingChanged(value: String) = editor.update { it.copy(billingAddress = value, errorMessage = null) }

    fun saveCard() {
        val state = uiState.value
        val validationError = when {
            state.cardholderName.isBlank() -> "Cardholder name is required."
            !Validators.isValidCardNumber(state.cardNumber) -> "Enter a valid card number."
            state.expiryDate.length < 4 -> "Enter a valid expiry date."
            state.billingAddress.isBlank() -> "Billing address is required."
            else -> null
        }
        if (validationError != null) {
            editor.update { it.copy(errorMessage = validationError) }
            return
        }
        val userId = AppContainer.authRepository.currentUser.value.id
        AppContainer.accountSettingsRepository.savePaymentMethod(
            userId = userId,
            entry = PaymentCardEntry(
                id = "card_${System.currentTimeMillis()}",
                cardholderName = state.cardholderName,
                cardNumberMasked = "•••• ${state.cardNumber.takeLast(4)}",
                expiryDate = state.expiryDate,
                billingAddress = state.billingAddress,
            ),
        )
        editor.value = PaymentMethodsUiState(cards = AppContainer.accountSettingsRepository.getPaymentMethods(userId))
    }
}

@Composable
fun PaymentMethodsScreen(
    onBack: () -> Unit,
    viewModel: PaymentMethodsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScaffold(title = "Payment Methods", onBack = onBack) { padding ->
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
                    title = "Your payment information is encrypted and securely stored.",
                    subtitle = "Save cards for a seamless checkout while keeping billing details clear and professional.",
                    icon = Icons.Outlined.VerifiedUser,
                )
            }
            item {
                SettingsCard("Add Card") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AuthTextField("Cardholder Name", uiState.cardholderName, viewModel::onCardholderChanged, placeholder = "Enter cardholder name")
                        AuthTextField("Card Number", uiState.cardNumber, viewModel::onCardNumberChanged, placeholder = "Enter card number")
                        AuthTextField("Expiry Date", uiState.expiryDate, viewModel::onExpiryChanged, placeholder = "MM/YY")
                        AuthTextField("Billing Address", uiState.billingAddress, viewModel::onBillingChanged, placeholder = "Enter billing address")
                        uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                        PrimaryButton(text = "Add Card", onClick = viewModel::saveCard)
                    }
                }
            }
            if (uiState.cards.isEmpty()) {
                item {
                    SettingsCard("Saved Cards") {
                        Text("No saved cards yet.", style = MaterialTheme.typography.bodyMedium, color = FloraTextSecondary)
                    }
                }
            } else {
                items(uiState.cards, key = PaymentCardEntry::id) { card ->
                    SettingsCard(card.cardNumberMasked) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(card.cardholderName, style = MaterialTheme.typography.titleMedium, color = FloraText)
                            Text("Expiry ${card.expiryDate}", style = MaterialTheme.typography.bodyMedium, color = FloraTextSecondary)
                            Text(card.billingAddress, style = MaterialTheme.typography.bodySmall, color = FloraTextSecondary)
                        }
                    }
                }
            }
        }
    }
}
