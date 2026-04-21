package com.example.myappmobile.presentation.profile.seller

enum class SalesPeriodUi(val label: String) {
    WEEK("7D"),
    MONTH("1M"),
    THREE_MONTHS("3M"),
    SIX_MONTHS("6M"),
    YEAR("1Y"),
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

data class MonthlySellerStatisticUi(
    val monthLabel: String,
    val revenue: Double,
    val orders: Int,
    val balanceSnapshot: Double,
    val isClosedMonth: Boolean,
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
    val sellerName: String = "",
    val walletSummary: WalletSummaryUi = WalletSummaryUi(),
    val selectedPeriod: SalesPeriodUi = SalesPeriodUi.WEEK,
    val chartData: List<SalesBarUi> = emptyList(),
    val performanceHeadline: String = "",
    val monthlyStatistics: List<MonthlySellerStatisticUi> = emptyList(),
    val transferRecords: List<TransferRecordUi> = emptyList(),
)
