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

class PaymentsPayoutsViewModel : ViewModel() {

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
        SalesPeriodUi.THREE_MONTHS to listOf(
            SalesBarUi("Jan", 2850f),
            SalesBarUi("Feb", 3140f),
            SalesBarUi("Mar", 3360f),
        ),
        SalesPeriodUi.YEAR to listOf(
            SalesBarUi("Q1", 9350f),
            SalesBarUi("Q2", 10240f),
            SalesBarUi("Q3", 11880f),
            SalesBarUi("Q4", 12460f),
        ),
    )

    private val selectedPeriod = MutableStateFlow(SalesPeriodUi.WEEK)

    val uiState: StateFlow<PaymentsPayoutsUiState> = combine(
        AppContainer.authRepository.currentUser,
        AppContainer.uiPreferencesRepository.sellerWallets,
        selectedPeriod,
    ) { user, _, period ->
        val wallet = AppContainer.uiPreferencesRepository.getSellerWallet(user.id)
        PaymentsPayoutsUiState(
            walletSummary = WalletSummaryUi(
                availableBalance = wallet.availableBalance,
                pendingBalance = wallet.pendingBalance,
                totalWithdrawn = wallet.totalWithdrawn,
            ),
            selectedPeriod = period,
            chartData = chartByPeriod.getValue(period),
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
}
