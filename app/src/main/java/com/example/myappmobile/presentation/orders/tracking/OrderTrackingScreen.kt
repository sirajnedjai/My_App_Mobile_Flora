package com.example.myappmobile.presentation.orders.tracking

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.myappmobile.core.components.FloraRemoteImage
import com.example.myappmobile.R
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraCardBg
import com.example.myappmobile.core.theme.FloraDivider
import com.example.myappmobile.core.theme.FloraSelectedCard
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.StatusAmber
import com.example.myappmobile.core.theme.StatusBlue
import com.example.myappmobile.core.theme.StatusGreen
import com.example.myappmobile.core.theme.StatusRed
import com.example.myappmobile.domain.model.Order
import com.example.myappmobile.domain.model.OrderItem
import com.example.myappmobile.domain.model.OrderStatus

@Composable
fun OrderTrackingScreen(
    onBack: () -> Unit = {},
    onContinueShopping: () -> Unit = {},
    onOpenOrder: (String) -> Unit = {},
    viewModel: OrderTrackingViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = FloraBeige,
        topBar = { OrderTrackingTopBar(onBack = onBack) },
    ) { paddingValues ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.orders_loading), color = FloraTextSecondary)
            }

            uiState.orders.isEmpty() -> EmptyTrackingState(
                modifier = Modifier.padding(paddingValues),
                onContinueShopping = onContinueShopping,
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FloraBeige)
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    TrackingHeroCard(customerName = uiState.customerName, isSellerView = uiState.isSellerView)
                }
                uiState.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
                    item {
                        ErrorCard(message = message, onRetry = viewModel::refresh)
                    }
                }
                items(uiState.orders, key = Order::id) { order ->
                    OrderTrackingCard(order = order, onClick = { onOpenOrder(order.id) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderTrackingTopBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.profile_track_my_orders),
                style = MaterialTheme.typography.headlineMedium,
                color = FloraText,
            )
        },
        navigationIcon = {
            CircularIconButton(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.common_back),
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
private fun TrackingHeroCard(customerName: String, isSellerView: Boolean) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard.copy(alpha = 0.94f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(16.dp),
                color = FloraCardBg,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                        contentDescription = null,
                        tint = FloraBrown,
                    )
                }
            }
            Column {
                Text(
                    text = if (isSellerView) {
                        "Order activity for ${customerName.ifBlank { stringResource(R.string.orders_your_account) }}"
                    } else {
                        stringResource(
                            R.string.orders_progress_for,
                            customerName.ifBlank { stringResource(R.string.orders_your_account) },
                        )
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = FloraText,
                )
                Text(
                    text = stringResource(R.string.orders_progress_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun OrderTrackingCard(order: Order, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
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
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.orders_order_reference, order.reference),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = FloraText,
                    )
                    Text(
                        text = stringResource(
                            R.string.orders_placed_date,
                            order.placedDate.ifBlank { stringResource(R.string.common_recently) },
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = FloraTextSecondary,
                    )
                }
                StatusPill(status = order.status)
            }

            order.items.forEach { item ->
                TrackingItemRow(item = item, orderStatus = order.status)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                InfoColumn(label = stringResource(R.string.orders_delivery), value = order.estimatedDelivery.ifBlank { stringResource(R.string.orders_preparing_timing) })
                InfoColumn(
                    label = if (order.carrier.isNotBlank()) "Carrier" else stringResource(R.string.common_shipping),
                    value = if (order.carrier.isNotBlank()) order.carrier else order.shippingMethod.ifBlank { stringResource(R.string.orders_standard_delivery) },
                )
                InfoColumn(label = stringResource(R.string.common_total), value = "$${"%.2f".format(order.total)}")
            }
            if (order.trackingNumber.isNotBlank() || order.shipmentStatus.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (order.trackingNumber.isNotBlank()) {
                        InfoColumn(label = "Tracking", value = order.trackingNumber)
                    }
                    if (order.shipmentStatus.isNotBlank()) {
                        InfoColumn(label = "Shipment Status", value = order.shipmentStatus.replace('_', ' '))
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FloraCardBg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(message, color = FloraTextSecondary, style = MaterialTheme.typography.bodySmall)
            PrimaryButton(text = "Retry", onClick = onRetry)
        }
    }
}

@Composable
private fun TrackingItemRow(
    item: OrderItem,
    orderStatus: OrderStatus,
) {
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
            Text(
                text = item.product.name,
                style = MaterialTheme.typography.titleMedium,
                color = FloraText,
            )
            Text(
                text = stringResource(R.string.orders_qty_count, item.quantity),
                style = MaterialTheme.typography.bodySmall,
                color = FloraTextSecondary,
            )
            Text(
                text = item.variant.ifBlank { item.product.category.ifBlank { stringResource(R.string.orders_atelier_selection) } },
                style = MaterialTheme.typography.bodySmall,
                color = FloraTextSecondary,
            )
            Text(
                text = stringResource(R.string.orders_product_status, productStatusLabel(orderStatus)),
                style = MaterialTheme.typography.bodySmall,
                color = FloraBrown,
            )
        }
    }
}

@Composable
private fun StatusPill(status: OrderStatus) {
    val color = when (status) {
        OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.HAND_CRAFTED -> StatusAmber
        OrderStatus.SHIPPED -> StatusBlue
        OrderStatus.DELIVERED -> StatusGreen
        OrderStatus.CANCELLED -> StatusRed
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.16f),
    ) {
        Text(
            text = localizedOrderStatus(status),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = color,
        )
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = FloraTextSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = FloraText,
        )
    }
}

@Composable
private fun EmptyTrackingState(
    modifier: Modifier = Modifier,
    onContinueShopping: () -> Unit,
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
        Text(
            text = stringResource(R.string.orders_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            color = FloraText,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.orders_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = FloraTextSecondary,
        )
        Spacer(modifier = Modifier.height(20.dp))
        PrimaryButton(
            text = stringResource(R.string.common_continue_shopping),
            onClick = onContinueShopping,
        )
    }
}

@Composable
private fun localizedOrderStatus(status: OrderStatus): String = when (status) {
    OrderStatus.PENDING -> stringResource(R.string.status_pending)
    OrderStatus.CONFIRMED -> stringResource(R.string.status_confirmed)
    OrderStatus.HAND_CRAFTED -> stringResource(R.string.status_handcrafted)
    OrderStatus.SHIPPED -> stringResource(R.string.status_shipped)
    OrderStatus.DELIVERED -> stringResource(R.string.status_delivered)
    OrderStatus.CANCELLED -> stringResource(R.string.status_cancelled)
}

@Composable
private fun productStatusLabel(orderStatus: OrderStatus): String = when (orderStatus) {
    OrderStatus.PENDING -> stringResource(R.string.orders_status_awaiting_confirmation)
    OrderStatus.CONFIRMED -> stringResource(R.string.orders_status_materials_reserved)
    OrderStatus.HAND_CRAFTED -> stringResource(R.string.orders_status_hand_finished)
    OrderStatus.SHIPPED -> stringResource(R.string.orders_status_in_transit)
    OrderStatus.DELIVERED -> stringResource(R.string.status_delivered)
    OrderStatus.CANCELLED -> stringResource(R.string.orders_status_cancelled)
}
