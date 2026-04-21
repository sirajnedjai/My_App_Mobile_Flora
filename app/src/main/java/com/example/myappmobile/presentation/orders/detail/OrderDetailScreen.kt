package com.example.myappmobile.presentation.orders.detail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.components.FloraRemoteImage
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraCardBg
import com.example.myappmobile.core.theme.FloraSelectedCard
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.domain.model.Order

@Composable
fun OrderDetailScreen(
    onBack: () -> Unit = {},
    onRetry: () -> Unit = {},
    onTrackShipment: (String) -> Unit = {},
    viewModel: OrderDetailViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = FloraBeige,
        topBar = { OrderDetailTopBar(onBack = onBack) },
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

            uiState.order == null -> EmptyOrderDetailState(
                modifier = Modifier.padding(paddingValues),
                message = uiState.errorMessage ?: "This order is unavailable.",
                onRetry = onRetry,
            )

            else -> {
                val order = uiState.order ?: return@Scaffold
                OrderDetailContent(
                    order = order,
                    errorMessage = uiState.errorMessage,
                    modifier = Modifier.padding(paddingValues),
                    onRetry = onRetry,
                    onTrackShipment = { onTrackShipment(order.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderDetailTopBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Order Details",
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
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = FloraBeige,
            scrolledContainerColor = FloraBeige,
        ),
    )
}

@Composable
private fun OrderDetailContent(
    order: Order,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
    onTrackShipment: () -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FloraBeige),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DetailCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(order.reference.ifBlank { "FLORA Order" }, style = MaterialTheme.typography.headlineSmall, color = FloraText)
                    Text("Status: ${order.status.label()}", color = FloraBrown)
                    Text("Placed: ${order.placedDate.ifBlank { "Recently" }}", color = FloraTextSecondary)
                    if (!errorMessage.isNullOrBlank()) {
                        Text(errorMessage, color = FloraBrown, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            DetailCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Delivery & Payment", style = MaterialTheme.typography.titleMedium, color = FloraText)
                    InfoRow("Shipping", order.shippingMethod.ifBlank { "Not provided" })
                    InfoRow("Payment", order.paymentMethod.ifBlank { "Not provided" })
                    InfoRow("ETA", order.estimatedDelivery.ifBlank { "Awaiting update" })
                }
            }
        }

        item {
            DetailCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Shipping Information", style = MaterialTheme.typography.titleMedium, color = FloraText)
                    order.shippingAddress?.let { address ->
                        InfoRow("Recipient", address.fullName.ifBlank { order.customerName.ifBlank { "Not provided" } })
                        InfoRow("Phone", address.phoneNumber.ifBlank { order.customerPhone.ifBlank { "Not provided" } })
                        InfoRow(
                            "Address",
                            listOf(
                                address.street,
                                address.neighborhood,
                                address.municipality,
                                address.state,
                                address.postalCode,
                                address.country,
                            ).filter { it.isNotBlank() }.joinToString(", ").ifBlank { "Not provided" },
                        )
                    } ?: Text("Shipping address is not available.", color = FloraTextSecondary)
                }
            }
        }

        item {
            DetailCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Items", style = MaterialTheme.typography.titleMedium, color = FloraText)
                    order.items.forEach { item ->
                        OrderDetailItemRow(item = item)
                    }
                }
            }
        }

        item {
            DetailCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Summary", style = MaterialTheme.typography.titleMedium, color = FloraText)
                    InfoRow("Subtotal", "$${"%.2f".format(order.subtotal)}")
                    InfoRow("Shipping", "$${"%.2f".format(order.shippingCost)}")
                    InfoRow("Tax", "$${"%.2f".format(order.tax)}")
                    InfoRow("Total", "$${"%.2f".format(order.total)}")
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton(
                    text = "Track Shipment",
                    onClick = onTrackShipment,
                    modifier = Modifier.weight(1f),
                )
                PrimaryButton(
                    text = "Refresh",
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun OrderDetailItemRow(item: com.example.myappmobile.domain.model.OrderItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        FloraRemoteImage(
            imageUrl = item.product.imageUrl,
            contentDescription = item.product.name,
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(FloraCardBg),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(item.product.name, style = MaterialTheme.typography.titleMedium, color = FloraText)
            Text("Qty ${item.quantity}", color = FloraTextSecondary, style = MaterialTheme.typography.bodySmall)
            Text(item.variant.ifBlank { item.product.category.ifBlank { "FLORA selection" } }, color = FloraTextSecondary, style = MaterialTheme.typography.bodySmall)
            Text("Unit: $${"%.2f".format(item.unitPrice)}", color = FloraTextSecondary, style = MaterialTheme.typography.bodySmall)
            Text("Line total: $${"%.2f".format(item.lineTotal)}", color = FloraText, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DetailCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(label, color = FloraTextSecondary, style = MaterialTheme.typography.bodySmall)
        Text(value, color = FloraText, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f).padding(start = 12.dp))
    }
}

@Composable
private fun EmptyOrderDetailState(
    modifier: Modifier = Modifier,
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.LocalShipping,
            contentDescription = null,
            tint = FloraBrown,
            modifier = Modifier.size(40.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Order unavailable", style = MaterialTheme.typography.headlineSmall, color = FloraText)
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = FloraTextSecondary)
        Spacer(modifier = Modifier.height(20.dp))
        PrimaryButton(text = "Try Again", onClick = onRetry)
    }
}
