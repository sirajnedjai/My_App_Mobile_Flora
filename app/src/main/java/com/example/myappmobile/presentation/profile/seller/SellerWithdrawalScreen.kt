package com.example.myappmobile.presentation.profile.seller

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.R
import com.example.myappmobile.core.components.AuthTextField
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraCardBg
import com.example.myappmobile.core.theme.FloraError
import com.example.myappmobile.core.theme.FloraSelectedCard
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary

@Composable
fun SellerWithdrawalScreen(
    onBack: () -> Unit = {},
    viewModel: SellerWithdrawalViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.completedTransactionId) {
        if (uiState.completedTransactionId != null) {
            Toast.makeText(
                context,
                uiState.successMessage ?: context.getString(R.string.withdrawal_success_message),
                Toast.LENGTH_SHORT,
            ).show()
            onBack()
        }
    }

    Scaffold(
        containerColor = FloraBeige,
        topBar = {
            SellerSettingsTopBar(
                title = stringResource(R.string.withdrawal_screen_title),
                onBack = onBack,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(FloraBeige)
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                WithdrawalBalanceCard(availableBalance = uiState.availableBalance)
            }

            item {
                WithdrawalFormCard(
                    uiState = uiState,
                    onAmountChange = viewModel::onAmountChange,
                    onPaymentMethodSelected = viewModel::onPaymentMethodSelected,
                    onCardHolderNameChange = viewModel::onCardHolderNameChange,
                    onCardNumberChange = viewModel::onCardNumberChange,
                    onExpiryDateChange = viewModel::onExpiryDateChange,
                    onCvvChange = viewModel::onCvvChange,
                    onSubmit = viewModel::submitWithdrawal,
                )
            }
        }
    }
}

@Composable
private fun WithdrawalBalanceCard(availableBalance: Double) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = FloraBrown),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.withdrawal_available_balance),
                style = MaterialTheme.typography.titleMedium,
                color = FloraBeige.copy(alpha = 0.84f),
            )
            Text(
                text = "$${"%.2f".format(availableBalance)}",
                style = MaterialTheme.typography.displaySmall,
                color = FloraBeige,
            )
            Text(
                text = stringResource(R.string.withdrawal_balance_hint),
                style = MaterialTheme.typography.bodySmall,
                color = FloraBeige.copy(alpha = 0.82f),
            )
        }
    }
}

@Composable
private fun WithdrawalFormCard(
    uiState: SellerWithdrawalUiState,
    onAmountChange: (String) -> Unit,
    onPaymentMethodSelected: (WithdrawalPaymentMethodUi) -> Unit,
    onCardHolderNameChange: (String) -> Unit,
    onCardNumberChange: (String) -> Unit,
    onExpiryDateChange: (String) -> Unit,
    onCvvChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.withdrawal_form_title),
                style = MaterialTheme.typography.titleLarge,
                color = FloraText,
            )
            Text(
                text = stringResource(R.string.withdrawal_form_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = FloraTextSecondary,
            )

            AuthTextField(
                label = stringResource(R.string.withdrawal_amount_label),
                value = uiState.amount,
                onValueChange = onAmountChange,
                placeholder = stringResource(R.string.withdrawal_amount_placeholder),
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next,
                isError = uiState.amountError != null,
                errorMessage = uiState.amountError,
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.withdrawal_payment_method),
                    style = MaterialTheme.typography.labelSmall,
                    color = FloraTextSecondary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    WithdrawalMethodOption(
                        label = stringResource(R.string.withdrawal_method_bank_transfer),
                        selected = uiState.selectedMethod == WithdrawalPaymentMethodUi.BANK_TRANSFER,
                        onClick = { onPaymentMethodSelected(WithdrawalPaymentMethodUi.BANK_TRANSFER) },
                        modifier = Modifier.weight(1f),
                    )
                    WithdrawalMethodOption(
                        label = stringResource(R.string.withdrawal_method_cash),
                        selected = uiState.selectedMethod == WithdrawalPaymentMethodUi.CASH,
                        onClick = { onPaymentMethodSelected(WithdrawalPaymentMethodUi.CASH) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (uiState.selectedMethod == WithdrawalPaymentMethodUi.BANK_TRANSFER) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    AuthTextField(
                        label = stringResource(R.string.withdrawal_card_holder_label),
                        value = uiState.cardHolderName,
                        onValueChange = onCardHolderNameChange,
                        placeholder = stringResource(R.string.withdrawal_card_holder_placeholder),
                        isError = uiState.cardHolderNameError != null,
                        errorMessage = uiState.cardHolderNameError,
                    )
                    AuthTextField(
                        label = stringResource(R.string.withdrawal_card_number_label),
                        value = uiState.cardNumber,
                        onValueChange = onCardNumberChange,
                        placeholder = stringResource(R.string.withdrawal_card_number_placeholder),
                        keyboardType = KeyboardType.Number,
                        isError = uiState.cardNumberError != null,
                        errorMessage = uiState.cardNumberError,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AuthTextField(
                            label = stringResource(R.string.withdrawal_expiry_date_label),
                            value = uiState.expiryDate,
                            onValueChange = onExpiryDateChange,
                            placeholder = stringResource(R.string.withdrawal_expiry_date_placeholder),
                            keyboardType = KeyboardType.Number,
                            isError = uiState.expiryDateError != null,
                            errorMessage = uiState.expiryDateError,
                            modifier = Modifier.weight(1f),
                        )
                        AuthTextField(
                            label = stringResource(R.string.withdrawal_cvv_label),
                            value = uiState.cvv,
                            onValueChange = onCvvChange,
                            placeholder = stringResource(R.string.withdrawal_cvv_placeholder),
                            keyboardType = KeyboardType.NumberPassword,
                            isError = uiState.cvvError != null,
                            errorMessage = uiState.cvvError,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            uiState.generalErrorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraError,
                )
            }

            PrimaryButton(
                text = if (uiState.isSubmitting) {
                    stringResource(R.string.withdrawal_submitting)
                } else {
                    stringResource(R.string.withdrawal_confirm_button)
                },
                onClick = onSubmit,
                isLoading = uiState.isSubmitting,
            )
        }
    }
}

@Composable
private fun WithdrawalMethodOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) FloraBrown else FloraCardBg,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) FloraBeige else FloraText,
            )
        }
    }
}
