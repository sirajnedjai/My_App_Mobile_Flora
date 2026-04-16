package com.example.myappmobile.presentation.profile.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SellerWithdrawalViewModel : ViewModel() {

    private val editorState = MutableStateFlow(SellerWithdrawalUiState())

    val uiState: StateFlow<SellerWithdrawalUiState> = combine(
        AppContainer.authRepository.currentUser,
        AppContainer.uiPreferencesRepository.sellerWallets,
        editorState,
    ) { user, _, editor ->
        editor.copy(
            availableBalance = AppContainer.uiPreferencesRepository.getSellerWallet(user.id).availableBalance,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SellerWithdrawalUiState(),
    )

    fun onAmountChange(value: String) {
        val filtered = value.filterIndexed { index, char ->
            char.isDigit() || (char == '.' && index == value.indexOf('.'))
        }
        editorState.update {
            it.copy(
                amount = filtered,
                amountError = null,
                generalErrorMessage = null,
                successMessage = null,
                completedTransactionId = null,
            )
        }
    }

    fun onPaymentMethodSelected(method: WithdrawalPaymentMethodUi) {
        editorState.update {
            it.copy(
                selectedMethod = method,
                cardHolderNameError = null,
                cardNumberError = null,
                expiryDateError = null,
                cvvError = null,
                generalErrorMessage = null,
                successMessage = null,
                completedTransactionId = null,
            )
        }
    }

    fun onCardHolderNameChange(value: String) = updateCardEditor { copy(cardHolderName = value, cardHolderNameError = null) }

    fun onCardNumberChange(value: String) = updateCardEditor {
        copy(cardNumber = value.filter(Char::isDigit).take(16), cardNumberError = null)
    }

    fun onExpiryDateChange(value: String) {
        val digits = value.filter(Char::isDigit).take(4)
        val formatted = when {
            digits.length <= 2 -> digits
            else -> "${digits.take(2)}/${digits.drop(2)}"
        }
        updateCardEditor { copy(expiryDate = formatted, expiryDateError = null) }
    }

    fun onCvvChange(value: String) = updateCardEditor {
        copy(cvv = value.filter(Char::isDigit).take(4), cvvError = null)
    }

    fun submitWithdrawal() {
        val snapshot = uiState.value
        val amount = snapshot.amount.toDoubleOrNull()
        var amountError: String? = null
        var cardHolderNameError: String? = null
        var cardNumberError: String? = null
        var expiryDateError: String? = null
        var cvvError: String? = null

        when {
            amount == null -> amountError = "Enter a valid withdrawal amount."
            amount <= 0.0 -> amountError = "Amount must be greater than 0."
            amount > snapshot.availableBalance -> amountError = "Amount exceeds your available balance."
        }

        if (snapshot.selectedMethod == WithdrawalPaymentMethodUi.BANK_TRANSFER) {
            if (snapshot.cardHolderName.isBlank()) {
                cardHolderNameError = "Card holder name is required."
            }
            if (!CARD_NUMBER_REGEX.matches(snapshot.cardNumber)) {
                cardNumberError = "Enter a valid 16-digit card number."
            }
            if (!EXPIRY_DATE_REGEX.matches(snapshot.expiryDate) || !isValidExpiryMonth(snapshot.expiryDate)) {
                expiryDateError = "Enter a valid expiry date in MM/YY format."
            }
            if (!CVV_REGEX.matches(snapshot.cvv)) {
                cvvError = "Enter a valid 3 or 4 digit CVV."
            }
        }

        if (listOf(amountError, cardHolderNameError, cardNumberError, expiryDateError, cvvError).any { it != null }) {
            editorState.update {
                it.copy(
                    amountError = amountError,
                    cardHolderNameError = cardHolderNameError,
                    cardNumberError = cardNumberError,
                    expiryDateError = expiryDateError,
                    cvvError = cvvError,
                    generalErrorMessage = "Please review the highlighted fields.",
                    successMessage = null,
                    completedTransactionId = null,
                )
            }
            return
        }

        viewModelScope.launch {
            editorState.update {
                it.copy(
                    isSubmitting = true,
                    generalErrorMessage = null,
                    successMessage = null,
                    completedTransactionId = null,
                )
            }

            val updatedWallet = AppContainer.uiPreferencesRepository.processSellerWithdrawal(
                sellerId = AppContainer.authRepository.currentUser.value.id,
                amount = amount ?: 0.0,
                paymentMethod = paymentMethodLabel(snapshot.selectedMethod),
            )

            val transactionId = updatedWallet.transferRecords.firstOrNull()?.id
            editorState.update {
                it.copy(
                    amount = "",
                    cardHolderName = "",
                    cardNumber = "",
                    expiryDate = "",
                    cvv = "",
                    isSubmitting = false,
                    successMessage = "Withdrawal completed successfully.",
                    completedTransactionId = transactionId,
                )
            }
        }
    }

    private fun updateCardEditor(transform: SellerWithdrawalUiState.() -> SellerWithdrawalUiState) {
        editorState.update { current ->
            current.transform().copy(
                generalErrorMessage = null,
                successMessage = null,
                completedTransactionId = null,
            )
        }
    }

    private fun paymentMethodLabel(method: WithdrawalPaymentMethodUi): String = when (method) {
        WithdrawalPaymentMethodUi.BANK_TRANSFER -> "Bank Transfer"
        WithdrawalPaymentMethodUi.CASH -> "Cash"
    }

    private fun isValidExpiryMonth(expiryDate: String): Boolean {
        val month = expiryDate.substringBefore('/').toIntOrNull() ?: return false
        return month in 1..12
    }

    private companion object {
        val CARD_NUMBER_REGEX = Regex("^\\d{16}$")
        val EXPIRY_DATE_REGEX = Regex("^(0[1-9]|1[0-2])/\\d{2}$")
        val CVV_REGEX = Regex("^\\d{3,4}$")
    }
}
