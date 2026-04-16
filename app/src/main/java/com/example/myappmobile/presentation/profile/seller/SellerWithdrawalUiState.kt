package com.example.myappmobile.presentation.profile.seller

enum class WithdrawalPaymentMethodUi {
    BANK_TRANSFER,
    CASH,
}

data class SellerWithdrawalUiState(
    val availableBalance: Double = 0.0,
    val amount: String = "",
    val selectedMethod: WithdrawalPaymentMethodUi = WithdrawalPaymentMethodUi.BANK_TRANSFER,
    val cardHolderName: String = "",
    val cardNumber: String = "",
    val expiryDate: String = "",
    val cvv: String = "",
    val amountError: String? = null,
    val cardHolderNameError: String? = null,
    val cardNumberError: String? = null,
    val expiryDateError: String? = null,
    val cvvError: String? = null,
    val generalErrorMessage: String? = null,
    val successMessage: String? = null,
    val isSubmitting: Boolean = false,
    val completedTransactionId: String? = null,
)
