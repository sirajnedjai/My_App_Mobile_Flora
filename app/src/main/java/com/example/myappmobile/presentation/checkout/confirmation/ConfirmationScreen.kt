package com.example.myappmobile.presentation.checkout.confirmation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.components.PrimaryButton

@Composable
fun ConfirmationScreen(
    onReturnHome: () -> Unit = {},
    viewModel: ConfirmationViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.order

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Order Confirmed", style = MaterialTheme.typography.headlineMedium)
            Text("Reference: ${order?.reference.orEmpty()}")
            Text("Total: $${"%.2f".format(order?.total ?: 0.0)}")
            Text("Shipping: ${order?.shippingMethod.orEmpty()}")
            Text("Delivery: ${order?.estimatedDelivery.orEmpty()}")
            PrimaryButton(
                text = "Return Home",
                onClick = onReturnHome,
            )
        }
    }
}
