package com.example.myappmobile.presentation.seller.orders

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraCardBg
import com.example.myappmobile.core.theme.FloraError
import com.example.myappmobile.core.theme.FloraSelectedCard
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.StatusAmber
import com.example.myappmobile.core.theme.StatusAmberLight
import com.example.myappmobile.core.theme.StatusBlue
import com.example.myappmobile.core.theme.StatusBlueLight
import com.example.myappmobile.core.theme.StatusGreen
import com.example.myappmobile.core.theme.StatusGreenLight
import com.example.myappmobile.core.theme.StatusRed
import com.example.myappmobile.core.theme.StatusRedLight
import com.example.myappmobile.core.theme.White
import com.example.myappmobile.domain.model.Order
import com.example.myappmobile.domain.model.OrderStatus
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SellerOrderDetailScreen(
    onBack: () -> Unit,
    viewModel: SellerOrderDetailViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = FloraBeige,
        topBar = { SellerOrderDetailTopBar(onBack = onBack) },
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.order == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text("Loading order details...", color = FloraTextSecondary)
            }

            uiState.order == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(uiState.errorMessage ?: "Order details are not available.", color = FloraTextSecondary)
            }

            else -> SellerOrderDetailContent(
                uiState = uiState,
                order = requireNotNull(uiState.order),
                availableStatuses = uiState.availableStatuses,
                onUpdateStatus = viewModel::updateStatus,
                onRetry = viewModel::refresh,
                isUpdatingStatus = uiState.isUpdatingStatus,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerOrderDetailTopBar(
    onBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = "Order details",
                style = MaterialTheme.typography.headlineMedium,
                color = FloraText,
            )
        },
        navigationIcon = {
            CircularIconButton(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = FloraBeige,
            scrolledContainerColor = FloraBeige,
        ),
    )
}

@Composable
private fun SellerOrderDetailContent(
    uiState: SellerOrderDetailUiState,
    order: Order,
    availableStatuses: List<OrderStatus>,
    onUpdateStatus: (OrderStatus) -> Unit,
    onRetry: () -> Unit,
    isUpdatingStatus: Boolean,
    modifier: Modifier = Modifier,
) {
    var selectedStatus by remember(order.id, order.status) { mutableStateOf(order.status) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FloraBeige),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        uiState.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item {
                DetailCard(title = "Unable to refresh order") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(message, color = FloraTextSecondary)
                        PrimaryButton(
                            text = "Retry",
                            onClick = onRetry,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        uiState.successMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item {
                DetailCard(title = "Update confirmed") {
                    Text(message, color = FloraTextSecondary)
                }
            }
        }

        item {
            DetailCard(title = order.reference.ifBlank { "FLORA Order" }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LabeledDetail(
                        icon = Icons.Outlined.PersonOutline,
                        label = "Buyer",
                        value = order.customerName.ifBlank { "Unknown buyer" },
                    )
                    if (order.customerEmail.isNotBlank()) {
                        LabeledDetail(label = "Email", value = order.customerEmail)
                    }
                    if (order.customerPhone.isNotBlank()) {
                        LabeledDetail(label = "Phone", value = order.customerPhone)
                    }
                    CompactDetailTile(
                        icon = Icons.Outlined.CalendarToday,
                        label = "Placed",
                        value = order.placedDate.ifBlank { "Not available" },
                    )
                    CompactDetailTile(
                        icon = Icons.Outlined.Payments,
                        label = "Total",
                        value = formatMoney(order.total),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Current status", color = FloraTextSecondary)
                        SellerDetailStatusChip(status = order.status)
                    }
                }
            }
        }

        item {
            DetailCard(title = "Ordered products") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (order.items.isEmpty()) {
                        Text(
                            "The backend did not return item lines for this order yet.",
                            color = FloraTextSecondary,
                        )
                    } else {
                        order.items.forEach { item ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(item.product.name, style = MaterialTheme.typography.titleMedium, color = FloraText)
                                Text("Qty ${item.quantity}", color = FloraTextSecondary)
                                Text(item.variant.ifBlank { item.product.category.ifBlank { "FLORA selection" } }, color = FloraTextSecondary)
                                Text("Unit ${formatMoney(item.unitPrice)}", color = FloraTextSecondary)
                                Text(formatMoney(item.lineTotal), color = FloraText, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                    Surface(shape = RoundedCornerShape(18.dp), color = FloraCardBg) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Subtotal ${formatMoney(order.subtotal)}", color = FloraTextSecondary)
                            Text("Shipping ${formatMoney(order.shippingCost)}", color = FloraTextSecondary)
                            if (order.tax > 0.0) {
                                Text("Tax ${formatMoney(order.tax)}", color = FloraTextSecondary)
                            }
                            Text("Seller total", color = FloraTextSecondary)
                            Text(formatMoney(order.total), style = MaterialTheme.typography.titleLarge, color = FloraText)
                        }
                    }
                }
            }
        }

        item {
            DetailCard(title = "Delivery") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledDetail(
                        icon = Icons.Outlined.LocalShipping,
                        label = "Method",
                        value = order.shippingMethod.ifBlank { "Not provided" },
                    )
                    if (order.paymentMethod.isNotBlank()) {
                        LabeledDetail(label = "Payment", value = order.paymentMethod)
                    }
                    LabeledDetail(label = "ETA", value = order.estimatedDelivery.ifBlank { "Awaiting status updates" })
                    order.shippingAddress?.let { address ->
                        Text(
                            listOf(address.fullName, address.street, address.neighborhood, address.municipality, address.state, address.postalCode, address.country)
                                .filter { it.isNotBlank() }
                                .joinToString("\n"),
                            color = FloraTextSecondary,
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        item {
            SellerWorkflowEtaCard(order = order)
        }

        item {
            SellerWorkflowControlCard(
                order = order,
                availableStatuses = availableStatuses,
                selectedStatus = selectedStatus,
                onSelectStatus = { selectedStatus = it },
                onUpdateStatus = onUpdateStatus,
                isUpdatingStatus = isUpdatingStatus,
            )
        }

        item {
            SellerStatusHistoryCard(order = order)
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = FloraText)
            content()
        }
    }
}

@Composable
private fun LabeledDetail(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        icon?.let {
            Icon(imageVector = it, contentDescription = null, tint = FloraBrown)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = FloraBrown)
            Text(value, color = FloraTextSecondary)
        }
    }
}

@Composable
private fun CompactDetailTile(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = White.copy(alpha = 0.72f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = FloraBrown)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = FloraBrown)
                Text(value, color = FloraText, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun StatusActionChip(
    status: OrderStatus,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = { Text(sellerStatusLabel(status)) },
        modifier = Modifier.fillMaxWidth(),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = FloraBrown.copy(alpha = 0.25f),
            selectedBorderColor = FloraBrown,
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = White.copy(alpha = 0.72f),
            labelColor = FloraText,
            selectedContainerColor = StatusAmberLight,
            selectedLabelColor = FloraText,
            disabledContainerColor = White.copy(alpha = 0.42f),
            disabledLabelColor = FloraTextSecondary,
        ),
    )
}

@Composable
private fun SellerWorkflowEtaCard(
    order: Order,
) {
    val (statusBackground, statusContent) = sellerStatusColors(order.status)
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.94f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "FULFILLMENT SNAPSHOT",
                style = MaterialTheme.typography.labelMedium,
                color = FloraBrown,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = order.estimatedDelivery.ifBlank { "Awaiting seller schedule" },
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = FloraText,
                    )
                    Text(
                        text = if (order.estimatedDelivery.isBlank()) {
                            "Set the next workflow step to guide the delivery timeline."
                        } else {
                            "Current logistics estimate shared across the workflow."
                        },
                        color = FloraTextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = statusBackground,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = statusContent,
                        )
                        Text(
                            text = sellerStatusLabel(order.status),
                            color = statusContent,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = FloraSelectedCard.copy(alpha = 0.92f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Workflow progress",
                        style = MaterialTheme.typography.labelLarge,
                        color = FloraText,
                    )
                    OrderStatus.values()
                        .filter { it != OrderStatus.CANCELLED }
                        .forEach { status ->
                            WorkflowStripRow(
                                status = status,
                                currentStatus = order.status,
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun WorkflowStripRow(
    status: OrderStatus,
    currentStatus: OrderStatus,
) {
    val allStatuses = listOf(
        OrderStatus.PENDING,
        OrderStatus.CONFIRMED,
        OrderStatus.SHIPPED,
        OrderStatus.DELIVERED,
    )
    val currentIndex = allStatuses.indexOf(
        when (currentStatus) {
            OrderStatus.HAND_CRAFTED -> OrderStatus.CONFIRMED
            else -> currentStatus
        },
    )
    val rowIndex = allStatuses.indexOf(status)
    val isComplete = currentStatus == OrderStatus.DELIVERED || (currentIndex >= 0 && rowIndex <= currentIndex)
    val isCurrent = rowIndex == currentIndex
    val accent = when {
        isCurrent -> FloraBrown
        isComplete -> StatusGreen
        else -> FloraBrown.copy(alpha = 0.22f)
    }
    val background = when {
        isCurrent -> StatusAmberLight
        isComplete -> StatusGreenLight
        else -> White.copy(alpha = 0.7f)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(accent, CircleShape),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = background,
            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = sellerStatusLabel(status),
                    color = FloraText,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = when {
                        isCurrent -> "Current"
                        isComplete -> "Completed"
                        else -> "Up next"
                    },
                    color = accent,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun SellerWorkflowControlCard(
    order: Order,
    availableStatuses: List<OrderStatus>,
    selectedStatus: OrderStatus,
    onSelectStatus: (OrderStatus) -> Unit,
    onUpdateStatus: (OrderStatus) -> Unit,
    isUpdatingStatus: Boolean,
) {
    val actionableStatuses = availableStatuses.distinct()
    val hasPendingSelection = selectedStatus != order.status
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.97f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "ORDER WORKFLOW",
                        style = MaterialTheme.typography.labelMedium,
                        color = FloraBrown,
                    )
                    Text(
                        text = "Update status",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = FloraText,
                    )
                    Text(
                        text = "Choose the next seller-facing milestone, then confirm the change. Delivery notifications are sent only when the order first becomes delivered.",
                        color = FloraTextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                SellerDetailStatusChip(status = order.status)
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = FloraCardBg,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SummaryMetric(
                        label = "Current status",
                        value = sellerStatusLabel(order.status),
                    )
                    SummaryMetric(
                        label = "Next action",
                        value = if (hasPendingSelection) sellerStatusLabel(selectedStatus) else "Select a new stage below",
                    )
                    SummaryMetric(
                        label = "Last update",
                        value = order.statusHistory.lastOrNull()?.timestamp
                            ?.ifBlank { order.placedDate.ifBlank { "Not available" } }
                            ?: order.placedDate.ifBlank { "Not available" },
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                actionableStatuses.forEach { status ->
                    StatusOptionCard(
                        status = status,
                        currentStatus = order.status,
                        selected = selectedStatus == status,
                        enabled = !isUpdatingStatus,
                        onClick = { onSelectStatus(status) },
                    )
                }
            }

            PrimaryButton(
                text = statusActionLabel(selectedStatus, order.status),
                onClick = { onUpdateStatus(selectedStatus) },
                enabled = hasPendingSelection && !isUpdatingStatus,
                isLoading = isUpdatingStatus,
                modifier = Modifier.fillMaxWidth(),
            )

            if (!hasPendingSelection) {
                Text(
                    text = "Select a new workflow stage to enable the update action.",
                    color = FloraTextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = FloraBrown,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = FloraText,
        )
    }
}

@Composable
private fun StatusOptionCard(
    status: OrderStatus,
    currentStatus: OrderStatus,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val (background, content) = sellerStatusColors(status)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = if (selected) background else White.copy(alpha = 0.86f),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 1.4.dp else 1.dp,
            color = if (selected) content.copy(alpha = 0.55f) else FloraBrown.copy(alpha = 0.12f),
        ),
        shadowElevation = if (selected) 4.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = sellerStatusLabel(status),
                    style = MaterialTheme.typography.titleMedium,
                    color = FloraText,
                )
                Text(
                    text = when {
                        status == currentStatus -> "This order is already at this stage."
                        status == OrderStatus.DELIVERED -> "Marks the workflow complete and notifies the buyer."
                        status == OrderStatus.CANCELLED -> "Use only if the order cannot continue."
                        status == OrderStatus.SHIPPED -> "Signals that the parcel is on its way to the buyer."
                        else -> "Advance the order to the next seller milestone."
                    },
                    color = FloraTextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Surface(
                shape = CircleShape,
                color = if (selected) content else FloraBrown.copy(alpha = 0.12f),
            ) {
                Box(
                    modifier = Modifier.size(34.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (selected) Icons.Outlined.CheckCircle else Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = if (selected) White else FloraBrown,
                    )
                }
            }
        }
    }
}

@Composable
private fun SellerStatusHistoryCard(
    order: Order,
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.94f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "STATUS HISTORY",
                style = MaterialTheme.typography.labelMedium,
                color = FloraBrown,
            )
            Text(
                text = "Timeline",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = FloraText,
            )
            Text(
                text = "Each update appears as a workflow event for the seller and buyer journey.",
                color = FloraTextSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                order.statusHistory.asReversed().forEachIndexed { index, entry ->
                    StatusTimelineRow(
                        status = entry.status,
                        timestamp = entry.timestamp,
                        note = entry.note,
                        showConnector = index != order.statusHistory.lastIndex,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusTimelineRow(
    status: OrderStatus,
    timestamp: String,
    note: String,
    showConnector: Boolean,
) {
    val (background, content) = sellerStatusColors(status)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(content, CircleShape),
            )
            if (showConnector) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(50.dp)
                        .background(content.copy(alpha = 0.24f)),
                )
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = background.copy(alpha = 0.65f),
            border = androidx.compose.foundation.BorderStroke(1.dp, content.copy(alpha = 0.24f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SellerDetailStatusChip(status = status)
                    Text(
                        text = timestamp.ifBlank { "Not available" },
                        color = FloraTextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    text = note.ifBlank { statusTimelineNote(status) },
                    color = FloraText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun SellerDetailStatusChip(
    status: OrderStatus,
) {
    val (background, content) = sellerStatusColors(status)

    Surface(
        shape = RoundedCornerShape(50),
        color = background,
    ) {
        Text(
            text = sellerStatusLabel(status),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = content,
        )
    }
}

private fun sellerStatusColors(status: OrderStatus): Pair<Color, Color> = when (status) {
    OrderStatus.PENDING -> StatusAmberLight to StatusAmber
    OrderStatus.CONFIRMED -> StatusBlueLight to StatusBlue
    OrderStatus.HAND_CRAFTED -> Color(0xFFF3E7FF) to Color(0xFF7040A0)
    OrderStatus.SHIPPED -> Color(0xFFF0EBE4) to FloraBrown
    OrderStatus.DELIVERED -> StatusGreenLight to StatusGreen
    OrderStatus.CANCELLED -> StatusRedLight to StatusRed
}

private fun sellerStatusLabel(status: OrderStatus): String = when (status) {
    OrderStatus.PENDING -> "Pending"
    OrderStatus.CONFIRMED -> "Processing"
    OrderStatus.HAND_CRAFTED -> "Processing"
    OrderStatus.SHIPPED -> "Shipped"
    OrderStatus.DELIVERED -> "Delivered"
    OrderStatus.CANCELLED -> "Cancelled"
}

private fun statusActionLabel(
    selectedStatus: OrderStatus,
    currentStatus: OrderStatus,
): String = when {
    selectedStatus == currentStatus -> "Select a new status"
    selectedStatus == OrderStatus.SHIPPED -> "Mark as Shipped"
    selectedStatus == OrderStatus.DELIVERED -> "Confirm Delivery"
    selectedStatus == OrderStatus.CANCELLED -> "Cancel Order"
    else -> "Confirm Status Update"
}

private fun statusTimelineNote(status: OrderStatus): String = when (status) {
    OrderStatus.PENDING -> "Order is waiting for seller confirmation."
    OrderStatus.CONFIRMED -> "Seller approved the order and moved it into processing."
    OrderStatus.HAND_CRAFTED -> "Production is actively in progress."
    OrderStatus.SHIPPED -> "Package has been handed off for delivery."
    OrderStatus.DELIVERED -> "Delivery has been completed successfully."
    OrderStatus.CANCELLED -> "Order was cancelled and removed from the active workflow."
}

private fun formatMoney(amount: Double): String =
    NumberFormat.getCurrencyInstance(Locale.US).format(amount)
