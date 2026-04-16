package com.example.myappmobile.presentation.checkout.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.components.PrimaryButton

private val paymentOptions = listOf(
    "Card ending in 4242",
    "PayPal",
    "Cash on Delivery",
)

@Composable
fun PaymentScreen(
    onPlaceOrder: () -> Unit = {},
    viewModel: PaymentViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { PaymentTopBar() }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Items: ${uiState.itemCount}")
            Text("Subtotal: $${"%.2f".format(uiState.subtotal)}")

            paymentOptions.forEach { option ->
                androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth()) {
                    RadioButton(
                        selected = uiState.paymentMethod == option,
                        onClick = { viewModel.onPaymentMethodSelected(option) },
                    )
                    Text(
                        text = option,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }

            PrimaryButton(
                text = "Place Order",
                onClick = { viewModel.placeOrder(onPlaceOrder) },
                enabled = uiState.itemCount > 0,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentTopBar() {
    TopAppBar(title = { Text("Payment") })
}
