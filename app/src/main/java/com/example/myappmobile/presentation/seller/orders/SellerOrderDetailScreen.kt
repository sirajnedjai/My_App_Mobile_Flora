package com.example.myappmobile.presentation.seller.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraCardBg
import com.example.myappmobile.core.theme.FloraSelectedCard
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.StatusAmber
import com.example.myappmobile.core.theme.StatusAmberLight
import com.example.myappmobile.domain.model.Order
import com.example.myappmobile.domain.model.OrderStatus

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
            uiState.isLoading -> Box(
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
                Text("Order details are not available.", color = FloraTextSecondary)
            }

            else -> SellerOrderDetailContent(
                order = requireNotNull(uiState.order),
                availableStatuses = uiState.availableStatuses,
                onUpdateStatus = viewModel::updateStatus,
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
    order: Order,
    availableStatuses: List<OrderStatus>,
    onUpdateStatus: (OrderStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FloraBeige),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DetailCard(title = order.reference) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Buyer: ${order.customerName.ifBlank { "Unknown buyer" }}", color = FloraText)
                    if (order.customerEmail.isNotBlank()) {
                        Text("Email: ${order.customerEmail}", color = FloraTextSecondary)
                    }
                    if (order.customerPhone.isNotBlank()) {
                        Text("Phone: ${order.customerPhone}", color = FloraTextSecondary)
                    }
                    Text("Placed: ${order.placedDate.ifBlank { "Not available" }}", color = FloraTextSecondary)
                    Text("Current status: ${sellerStatusLabel(order.status)}", color = FloraText)
                }
            }
        }

        item {
            DetailCard(title = "Ordered products") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.product.name, style = MaterialTheme.typography.titleMedium, color = FloraText)
                                Text("Qty ${item.quantity}", color = FloraTextSecondary)
                                Text(item.variant.ifBlank { item.product.category.ifBlank { "FLORA selection" } }, color = FloraTextSecondary)
                            }
                            Text("$${"%.2f".format(item.product.price * item.quantity)}", color = FloraText)
                        }
                    }
                    Surface(shape = RoundedCornerShape(18.dp), color = FloraCardBg) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Seller total", color = FloraTextSecondary)
                            Text("$${"%.2f".format(order.total)}", style = MaterialTheme.typography.titleLarge, color = FloraText)
                        }
                    }
                }
            }
        }

        item {
            DetailCard(title = "Delivery") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Method: ${order.shippingMethod.ifBlank { "Standard Delivery" }}", color = FloraText)
                    Text("ETA: ${order.estimatedDelivery.ifBlank { "Awaiting status updates" }}", color = FloraTextSecondary)
                    order.shippingAddress?.let { address ->
                        Text(
                            "Ship to: ${listOf(address.fullName, address.street, address.city, address.postalCode, address.country).filter { it.isNotBlank() }.joinToString(", ")}",
                            color = FloraTextSecondary,
                        )
                    }
                }
            }
        }

        item {
            DetailCard(title = "Update status") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Move the order through the seller workflow. A delivery notification will be sent only the first time the order becomes delivered.",
                        color = FloraTextSecondary,
                    )
                    availableStatuses.forEach { status ->
                        PrimaryButton(
                            text = sellerStatusLabel(status),
                            onClick = { onUpdateStatus(status) },
                            enabled = order.status != status,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        item {
            DetailCard(title = "Status history") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    order.statusHistory.asReversed().forEach { entry ->
                        Surface(shape = RoundedCornerShape(18.dp), color = StatusAmberLight.copy(alpha = 0.4f)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                Text(sellerStatusLabel(entry.status), color = FloraText, style = MaterialTheme.typography.titleSmall)
                                Text(entry.timestamp, color = FloraTextSecondary, style = MaterialTheme.typography.bodySmall)
                                if (entry.note.isNotBlank()) {
                                    Text(entry.note, color = FloraTextSecondary, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
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

private fun sellerStatusLabel(status: OrderStatus): String = when (status) {
    OrderStatus.PENDING -> "Pending"
    OrderStatus.CONFIRMED -> "Confirmed"
    OrderStatus.HAND_CRAFTED -> "Preparing"
    OrderStatus.SHIPPED -> "Shipped"
    OrderStatus.DELIVERED -> "Delivered"
    OrderStatus.CANCELLED -> "Cancelled"
}
