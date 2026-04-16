package com.example.myappmobile.presentation.seller.orders

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraError
import com.example.myappmobile.core.theme.FloraSuccess
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.SerifFontFamily
import com.example.myappmobile.core.theme.White
import com.example.myappmobile.domain.model.Order
import com.example.myappmobile.domain.model.OrderStatus

@Composable
fun SellerOrdersScreen(
    onBack: () -> Unit = {},
    onOpenOrder: (String) -> Unit = {},
    viewModel: SellerOrdersViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = FloraBeige,
        topBar = {
            SellerOrdersTopBar(onBack = onBack)
        },
    ) { paddingValues ->
        if (!uiState.isSeller) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Seller tools are available only for seller accounts.",
                    style = MaterialTheme.typography.titleMedium,
                    color = FloraTextSecondary,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FloraBeige)
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.84f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Received Orders",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = FloraText,
                            )
                            Text(
                                text = "Monitor incoming purchases for your FLORA store and track every status from pending to delivery.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FloraTextSecondary,
                            )
                        }
                    }
                }

                items(uiState.orders, key = { it.id }) { order ->
                    SellerOrderCard(order = order, onOpenOrder = onOpenOrder)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerOrdersTopBar(
    onBack: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Received Orders",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = SerifFontFamily,
                    fontStyle = FontStyle.Italic,
                ),
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
private fun SellerOrderCard(
    order: Order,
    onOpenOrder: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenOrder(order.id) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.86f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.reference,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = FloraText,
                    )
                    Text(
                        text = order.customerName.ifBlank { "Buyer not available" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = FloraTextSecondary,
                    )
                }
                StatusChip(status = order.status)
            }

            Text(
                text = order.items.joinToString(separator = ", ") { item ->
                    "${item.quantity}× ${item.product.name}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = FloraText,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DetailColumn(
                    label = "Placed",
                    value = order.placedDate.ifBlank { "Not available" },
                )
                DetailColumn(
                    label = "Total",
                    value = "$${"%.2f".format(order.total)}",
                )
                DetailColumn(
                    label = "Shipping",
                    value = order.shippingMethod.ifBlank { "Standard" },
                )
            }

            DetailColumn(
                label = "Delivery",
                value = order.estimatedDelivery.ifBlank { order.status.label() },
            )

            PrimaryButton(
                text = "Open order details",
                onClick = { onOpenOrder(order.id) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DetailColumn(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = FloraBrown,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = FloraText,
        )
    }
}

@Composable
private fun StatusChip(
    status: OrderStatus,
) {
    val (background, content) = when (status) {
        OrderStatus.PENDING -> Color(0xFFFFF0CC) to Color(0xFF9A6A00)
        OrderStatus.CONFIRMED -> Color(0xFFE7F0FF) to Color(0xFF285EA8)
        OrderStatus.HAND_CRAFTED -> Color(0xFFF3E7FF) to Color(0xFF7040A0)
        OrderStatus.SHIPPED -> Color(0xFFE4F5F0) to FloraSuccess
        OrderStatus.DELIVERED -> Color(0xFFDFF5E7) to Color(0xFF1F7A46)
        OrderStatus.CANCELLED -> Color(0xFFFCE5E6) to FloraError
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = background,
    ) {
        Text(
            text = status.label(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = content,
        )
    }
}
