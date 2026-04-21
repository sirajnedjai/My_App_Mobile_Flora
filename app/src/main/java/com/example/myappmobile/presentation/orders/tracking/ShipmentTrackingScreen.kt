package com.example.myappmobile.presentation.orders.tracking

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraCardBg
import com.example.myappmobile.core.theme.FloraSelectedCard
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.StatusAmber
import com.example.myappmobile.core.theme.StatusBlue
import com.example.myappmobile.core.theme.StatusGreen
import com.example.myappmobile.core.theme.StatusRed

@Composable
fun ShipmentTrackingScreen(
    onBack: () -> Unit = {},
    onRetry: () -> Unit = {},
    viewModel: ShipmentTrackingViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = FloraBeige,
        topBar = { ShipmentTrackingTopBar(onBack = onBack) },
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.order == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text("Loading shipment tracking...", color = FloraTextSecondary)
            }

            uiState.order == null -> ShipmentTrackingEmptyState(
                modifier = Modifier.padding(paddingValues),
                message = uiState.errorMessage ?: "Shipment information is unavailable.",
                onRetry = onRetry,
            )

            else -> {
                val order = uiState.order ?: return@Scaffold
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(FloraBeige)
                        .padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        TrackingSummaryCard(
                            reference = order.reference,
                            shippingMethod = order.shippingMethod,
                            estimatedDelivery = order.estimatedDelivery,
                            errorMessage = uiState.errorMessage,
                        )
                    }
                    item {
                        TrackingMetaCard(
                            shippingAddress = listOf(
                                order.shippingAddress?.street,
                                order.shippingAddress?.neighborhood,
                                order.shippingAddress?.municipality,
                                order.shippingAddress?.state,
                                order.shippingAddress?.country,
                            ).filter { !it.isNullOrBlank() }.joinToString(", ").ifBlank { "Shipping details are not available yet." },
                        )
                    }
                    itemsIndexed(uiState.stages) { index, stage ->
                        TrackingStageCard(
                            stage = stage,
                            isLast = index == uiState.stages.lastIndex,
                        )
                    }
                    item {
                        PrimaryButton(
                            text = "Refresh Tracking",
                            onClick = onRetry,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShipmentTrackingTopBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Shipment Tracking",
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
private fun TrackingSummaryCard(
    reference: String,
    shippingMethod: String,
    estimatedDelivery: String,
    errorMessage: String?,
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(reference.ifBlank { "FLORA Shipment" }, style = MaterialTheme.typography.headlineSmall, color = FloraText)
            Text("Shipping method: ${shippingMethod.ifBlank { "Not available" }}", color = FloraTextSecondary)
            Text("Estimated delivery: ${estimatedDelivery.ifBlank { "Awaiting update" }}", color = FloraTextSecondary)
            if (!errorMessage.isNullOrBlank()) {
                Text(errorMessage, color = FloraBrown, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TrackingMetaCard(shippingAddress: String) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = FloraCardBg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Delivery Address", style = MaterialTheme.typography.titleMedium, color = FloraText)
            Text(shippingAddress, style = MaterialTheme.typography.bodySmall, color = FloraTextSecondary)
        }
    }
}

@Composable
private fun TrackingStageCard(
    stage: ShipmentTrackingStage,
    isLast: Boolean,
) {
    val color = when (stage.state) {
        ShipmentTrackingStageState.COMPLETED -> StatusGreen
        ShipmentTrackingStageState.CURRENT -> StatusBlue
        ShipmentTrackingStageState.CANCELLED -> StatusRed
        ShipmentTrackingStageState.UPCOMING -> StatusAmber
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard.copy(alpha = 0.92f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(18.dp),
                    shape = CircleShape,
                    color = color,
                ) {}
                if (!isLast) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(width = 2.dp, height = 36.dp)
                            .background(color.copy(alpha = 0.4f)),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stage.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = FloraText,
                )
                Text(
                    stage.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun ShipmentTrackingEmptyState(
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
        Text("Tracking unavailable", style = MaterialTheme.typography.headlineSmall, color = FloraText)
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = FloraTextSecondary)
        Spacer(modifier = Modifier.height(20.dp))
        PrimaryButton(text = "Try Again", onClick = onRetry)
    }
}
