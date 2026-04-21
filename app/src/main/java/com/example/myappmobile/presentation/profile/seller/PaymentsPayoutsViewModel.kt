package com.example.myappmobile.presentation.profile.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class PaymentsPayoutsViewModel : ViewModel() {
    private val today = LocalDate.now()
    private val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
    private val monthCardFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    private val monthlyRevenue = listOf(
        YearMonth.now().minusMonths(11) to 1820f,
        YearMonth.now().minusMonths(10) to 1945f,
        YearMonth.now().minusMonths(9) to 2050f,
        YearMonth.now().minusMonths(8) to 2240f,
        YearMonth.now().minusMonths(7) to 2385f,
        YearMonth.now().minusMonths(6) to 2520f,
        YearMonth.now().minusMonths(5) to 2675f,
        YearMonth.now().minusMonths(4) to 2810f,
        YearMonth.now().minusMonths(3) to 2960f,
        YearMonth.now().minusMonths(2) to 3140f,
        YearMonth.now().minusMonths(1) to 3360f,
        YearMonth.now() to 3495f,
    )

    private val chartByPeriod = mapOf(
        SalesPeriodUi.WEEK to listOf(
            SalesBarUi("Mon", 120f),
            SalesBarUi("Tue", 90f),
            SalesBarUi("Wed", 156f),
            SalesBarUi("Thu", 188f),
            SalesBarUi("Fri", 134f),
            SalesBarUi("Sat", 210f),
            SalesBarUi("Sun", 172f),
        ),
        SalesPeriodUi.MONTH to listOf(
            SalesBarUi("W1", 860f),
            SalesBarUi("W2", 920f),
            SalesBarUi("W3", 1085f),
            SalesBarUi("W4", 970f),
        ),
        SalesPeriodUi.THREE_MONTHS to monthlyRevenue.takeLast(3).map { (month, amount) ->
            SalesBarUi(month.format(monthFormatter), amount)
        },
        SalesPeriodUi.SIX_MONTHS to monthlyRevenue.takeLast(6).map { (month, amount) ->
            SalesBarUi(month.format(monthFormatter), amount)
        },
        SalesPeriodUi.YEAR to monthlyRevenue.takeLast(12).map { (month, amount) ->
            SalesBarUi(month.format(monthFormatter), amount)
        },
    )

    private val selectedPeriod = MutableStateFlow(SalesPeriodUi.WEEK)

    val uiState: StateFlow<PaymentsPayoutsUiState> = combine(
        AppContainer.authRepository.currentUser,
        AppContainer.uiPreferencesRepository.sellerWallets,
        selectedPeriod,
    ) { user, _, period ->
        val wallet = AppContainer.uiPreferencesRepository.getSellerWallet(user.id)
        val visibleMonthlyCards = buildMonthlyStatisticCards(wallet.availableBalance)
        val recentPerformance = chartByPeriod.getValue(period)
        PaymentsPayoutsUiState(
            sellerName = user.fullName.ifBlank { "Seller" },
            walletSummary = WalletSummaryUi(
                availableBalance = wallet.availableBalance,
                pendingBalance = wallet.pendingBalance,
                totalWithdrawn = wallet.totalWithdrawn,
            ),
            selectedPeriod = period,
            chartData = recentPerformance,
            performanceHeadline = buildPerformanceHeadline(period, recentPerformance),
            monthlyStatistics = visibleMonthlyCards,
            transferRecords = wallet.transferRecords.map { record ->
                TransferRecordUi(
                    id = record.id,
                    date = record.date,
                    type = record.type,
                    status = record.status,
                    amountSent = record.amountSent,
                    orderNumber = record.orderNumber,
                    paymentMethod = record.paymentMethod,
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PaymentsPayoutsUiState(
            chartData = chartByPeriod.getValue(SalesPeriodUi.WEEK),
        ),
    )

    fun onPeriodSelected(period: SalesPeriodUi) {
        selectedPeriod.update { period }
    }

    private fun buildMonthlyStatisticCards(availableBalance: Double): List<MonthlySellerStatisticUi> {
        val shouldIncludeCurrentMonth = today.dayOfMonth == today.lengthOfMonth()
        val latestClosedMonth = if (shouldIncludeCurrentMonth) YearMonth.from(today) else YearMonth.from(today).minusMonths(1)
        return monthlyRevenue
            .filter { (month, _) -> month <= latestClosedMonth }
            .mapIndexed { index, (month, amount) ->
                MonthlySellerStatisticUi(
                    monthLabel = month.format(monthCardFormatter),
                    revenue = amount.toDouble(),
                    orders = 12 + index * 2,
                    balanceSnapshot = (availableBalance * 0.35) + amount * 0.18,
                    isClosedMonth = true,
                )
            }
            .reversed()
    }

    private fun buildPerformanceHeadline(
        period: SalesPeriodUi,
        chartData: List<SalesBarUi>,
    ): String {
        val peak = chartData.maxByOrNull { it.amount }
        return when (period) {
            SalesPeriodUi.WEEK -> "This week peaked on ${peak?.label.orEmpty()} with ${peak?.amount?.toInt() ?: 0} in sales."
            SalesPeriodUi.MONTH -> "Your monthly run rate is stable across all weeks."
            SalesPeriodUi.THREE_MONTHS -> "The last quarter shows consistent growth across each month."
            SalesPeriodUi.SIX_MONTHS -> "Six-month performance confirms steady demand and stronger repeat orders."
            SalesPeriodUi.YEAR -> "Yearly performance gives you a full twelve-month revenue view."
        }
    }
}
