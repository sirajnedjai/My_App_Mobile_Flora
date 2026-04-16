package com.example.myappmobile.presentation.profile.seller

enum class SalesPeriodUi(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    THREE_MONTHS("3 Months"),
    YEAR("Year"),
}

data class WalletSummaryUi(
    val availableBalance: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val totalWithdrawn: Double = 0.0,
)

data class SalesBarUi(
    val label: String,
    val amount: Float,
)

data class TransferRecordUi(
    val id: String,
    val date: String,
    val type: String,
    val status: String,
    val amountSent: Double,
    val orderNumber: String,
    val paymentMethod: String = "",
)

data class PaymentsPayoutsUiState(
    val walletSummary: WalletSummaryUi = WalletSummaryUi(),
    val selectedPeriod: SalesPeriodUi = SalesPeriodUi.WEEK,
    val chartData: List<SalesBarUi> = emptyList(),
    val transferRecords: List<TransferRecordUi> = emptyList(),
)
