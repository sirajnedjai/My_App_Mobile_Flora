package com.example.myappmobile.presentation.checkout.shipping

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

private val shippingOptions = listOf(
    "Standard Delivery",
    "Express Courier",
    "Concierge White-Glove",
)

@Composable
fun ShippingScreen(
    onContinue: () -> Unit = {},
    viewModel: ShippingViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { ShippingTopBar() }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            shippingOptions.forEach { option ->
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    RadioButton(
                        selected = uiState.shippingMethod == option,
                        onClick = { viewModel.onShippingMethodSelected(option) },
                    )
                    Text(
                        text = option,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }

            PrimaryButton(
                text = "Continue to Payment",
                onClick = { viewModel.save(onContinue) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShippingTopBar() {
    TopAppBar(title = { Text("Shipping Method") })
}
