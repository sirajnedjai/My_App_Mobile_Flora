package com.example.myappmobile.presentation.checkout.confirmation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraSuccess
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.presentation.checkout.FloraPrimaryButton
import com.example.myappmobile.presentation.checkout.SuccessInfoRow
import com.example.myappmobile.presentation.checkout.buildAddressSummary
import com.example.myappmobile.presentation.checkout.formatCurrency
import com.example.myappmobile.presentation.checkout.formatPaymentMethod
import com.example.myappmobile.presentation.checkout.formatShippingMethod

@Composable
fun ConfirmationScreen(
    onReturnHome: () -> Unit = {},
    viewModel: ConfirmationViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.order
    val address = order?.shippingAddress
    val addressSummary = address?.let {
        buildAddressSummary(
            state = it.state,
            municipality = it.municipality,
            neighborhood = it.neighborhood,
            streetAddress = it.street,
            postalCode = it.postalCode,
            country = it.country,
        )
    }.orEmpty()

    Scaffold(
        containerColor = FloraBeige,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .background(FloraSuccess.copy(alpha = 0.12f), CircleShape)
                    .padding(22.dp),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Order confirmed",
                    tint = FloraSuccess,
                    modifier = Modifier.padding(4.dp),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Order Confirmed",
                    style = MaterialTheme.typography.headlineMedium,
                    color = FloraText,
                )
                Text(
                    text = "Your FLORA order has been placed successfully and is now being prepared.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FloraTextSecondary,
                )
            }

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SuccessInfoRow(
                        label = "Reference",
                        value = order?.reference.orEmpty().ifBlank { "Pending" },
                    )
                    SuccessInfoRow(
                        label = "Total",
                        value = formatCurrency(order?.total ?: 0.0),
                    )
                    SuccessInfoRow(
                        label = "Shipping",
                        value = formatShippingMethod(order?.shippingMethod.orEmpty()),
                    )
                    if (!order?.paymentMethod.isNullOrBlank()) {
                        SuccessInfoRow(
                            label = "Payment",
                            value = formatPaymentMethod(order?.paymentMethod.orEmpty()),
                        )
                    }
                    if (!order?.estimatedDelivery.isNullOrBlank()) {
                        SuccessInfoRow(
                            label = "Delivery",
                            value = order?.estimatedDelivery.orEmpty(),
                        )
                    }
                    if (address != null && addressSummary.isNotBlank()) {
                        SuccessInfoRow(
                            label = "Address",
                            value = addressSummary,
                        )
                    }
                }
            }

            FloraPrimaryButton(
                text = "Return Home",
                onClick = onReturnHome,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
