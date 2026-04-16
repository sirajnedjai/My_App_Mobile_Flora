package com.example.myappmobile.presentation.profile.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SouthWest
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.R
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraCardBg
import com.example.myappmobile.core.theme.FloraSelectedCard
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.StoneGray

@Composable
fun PaymentsPayoutsScreen(
    onBack: () -> Unit = {},
    onWithdrawClick: () -> Unit = {},
    viewModel: PaymentsPayoutsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = FloraBeige,
        topBar = { SellerSettingsTopBar(title = stringResource(R.string.profile_payments_payouts), onBack = onBack) },
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
                WalletSummaryCard(
                    walletSummary = uiState.walletSummary,
                    onWithdrawClick = onWithdrawClick,
                )
            }

            item {
                SalesChartCard(
                    selectedPeriod = uiState.selectedPeriod,
                    chartData = uiState.chartData,
                    onPeriodSelected = viewModel::onPeriodSelected,
                )
            }

            item {
                TransferRecordsHeader()
            }

            items(uiState.transferRecords) { record ->
                TransferRecordRow(record = record)
            }
        }
    }
}

@Composable
private fun WalletSummaryCard(
    walletSummary: WalletSummaryUi,
    onWithdrawClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = FloraBrown),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.payout_wallet_summary),
                    style = MaterialTheme.typography.titleMedium,
                    color = FloraBeige.copy(alpha = 0.88f),
                )
                Text(
                    text = "$${"%.2f".format(walletSummary.availableBalance)}",
                    style = MaterialTheme.typography.displaySmall,
                    color = FloraBeige,
                )
                Text(
                    text = stringResource(R.string.payout_available_to_withdraw),
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraBeige.copy(alpha = 0.82f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                WalletMetricCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.payout_pending),
                    value = "$${"%.2f".format(walletSummary.pendingBalance)}",
                    icon = Icons.Outlined.Schedule,
                )
                WalletMetricCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.payout_withdrawn),
                    value = "$${"%.2f".format(walletSummary.totalWithdrawn)}",
                    icon = Icons.Outlined.SouthWest,
                )
            }

            PrimaryButton(
                text = stringResource(R.string.payout_withdraw_funds),
                onClick = onWithdrawClick,
            )
        }
    }
}

@Composable
private fun WalletMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = FloraBeige.copy(alpha = 0.14f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FloraBeige.copy(alpha = 0.16f),
            ) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = FloraBeige,
                    modifier = Modifier.padding(10.dp),
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = FloraBeige.copy(alpha = 0.82f),
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = FloraBeige,
                )
            }
        }
    }
}

@Composable
private fun SalesChartCard(
    selectedPeriod: SalesPeriodUi,
    chartData: List<SalesBarUi>,
    onPeriodSelected: (SalesPeriodUi) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.payout_sales_status),
                    style = MaterialTheme.typography.titleLarge,
                    color = FloraText,
                )
                Text(
                    text = stringResource(R.string.payout_sales_status_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SalesPeriodUi.entries.forEach { period ->
                    SalesFilterButton(
                        modifier = Modifier.weight(1f),
                        label = localizedSalesPeriod(period),
                        selected = period == selectedPeriod,
                        onClick = { onPeriodSelected(period) },
                    )
                }
            }

            SalesBarChart(chartData = chartData)
        }
    }
}

@Composable
private fun RowScope.SalesFilterButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(46.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) FloraBrown else FloraCardBg,
        tonalElevation = if (selected) 2.dp else 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) FloraBeige else FloraTextSecondary,
            )
        }
    }
}

@Composable
private fun SalesBarChart(chartData: List<SalesBarUi>) {
    val maxValue = (chartData.maxOfOrNull { it.amount } ?: 1f).coerceAtLeast(1f)
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = FloraCardBg,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(228.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            chartData.forEach { bar ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "$${bar.amount.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = StoneGray,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(18.dp),
                            color = FloraSelectedCard,
                        ) {}
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(bar.amount / maxValue),
                            shape = RoundedCornerShape(18.dp),
                            color = FloraBrown.copy(alpha = 0.82f),
                        ) {}
                    }
                    Text(
                        text = localizedSalesBarLabel(bar.label),
                        style = MaterialTheme.typography.labelMedium,
                        color = FloraText,
                    )
                }
            }
        }
    }
}

@Composable
private fun TransferRecordsHeader() {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.payout_transfer_history),
            style = MaterialTheme.typography.titleLarge,
            color = FloraText,
        )
        Text(
            text = stringResource(R.string.payout_transfer_history_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = FloraTextSecondary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = FloraCardBg)
    }
}

@Composable
private fun TransferRecordRow(record: TransferRecordUi) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = localizedTransferType(record.type),
                        style = MaterialTheme.typography.titleMedium,
                        color = FloraText,
                    )
                    Text(
                        text = record.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = FloraTextSecondary,
                    )
                }
                StatusBadge(status = localizedTransferStatus(record.status))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TransferMetric(
                    label = stringResource(R.string.common_date),
                    value = record.date,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                TransferMetric(
                    label = stringResource(R.string.payout_amount_sent),
                    value = "$${"%.2f".format(record.amountSent)}",
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TransferMetric(
                    label = stringResource(R.string.common_type),
                    value = localizedTransferType(record.type),
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                TransferMetric(
                    label = stringResource(R.string.payout_order_number),
                    value = record.orderNumber,
                    modifier = Modifier.weight(1f),
                )
            }

            if (record.paymentMethod.isNotBlank()) {
                TransferMetric(
                    label = stringResource(R.string.withdrawal_payment_method),
                    value = record.paymentMethod,
                )
            }
        }
    }
}

@Composable
private fun localizedSalesPeriod(period: SalesPeriodUi): String = when (period) {
    SalesPeriodUi.WEEK -> stringResource(R.string.period_week)
    SalesPeriodUi.MONTH -> stringResource(R.string.period_month)
    SalesPeriodUi.THREE_MONTHS -> stringResource(R.string.period_three_months)
    SalesPeriodUi.YEAR -> stringResource(R.string.period_year)
}

@Composable
private fun StatusBadge(status: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = when (status) {
            localizedTransferStatus("Completed") -> FloraBrown.copy(alpha = 0.14f)
            localizedTransferStatus("Processing") -> FloraCardBg
            else -> FloraSelectedCard
        },
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (status == "Completed") FloraBrown else FloraTextSecondary,
        )
    }
}

@Composable
private fun TransferMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = StoneGray,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = FloraText,
        )
    }
}

@Composable
private fun localizedTransferStatus(status: String): String = when (status) {
    "Completed" -> stringResource(R.string.status_completed)
    "Processing" -> stringResource(R.string.status_processing)
    else -> status
}

@Composable
private fun localizedTransferType(type: String): String = when (type) {
    "Weekly Payout" -> stringResource(R.string.transfer_type_weekly_payout)
    "Refund Adjustment" -> stringResource(R.string.transfer_type_refund_adjustment)
    "Monthly Bonus" -> stringResource(R.string.transfer_type_monthly_bonus)
    "Withdrawal" -> stringResource(R.string.transfer_type_withdrawal)
    else -> type
}

@Composable
private fun localizedSalesBarLabel(label: String): String = when (label) {
    "Mon" -> stringResource(R.string.day_mon)
    "Tue" -> stringResource(R.string.day_tue)
    "Wed" -> stringResource(R.string.day_wed)
    "Thu" -> stringResource(R.string.day_thu)
    "Fri" -> stringResource(R.string.day_fri)
    "Sat" -> stringResource(R.string.day_sat)
    "Sun" -> stringResource(R.string.day_sun)
    else -> label
}
