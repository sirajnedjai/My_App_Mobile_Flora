package com.example.myappmobile.presentation.checkout.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.presentation.checkout.CheckoutOptionCard
import com.example.myappmobile.presentation.checkout.CheckoutSectionCard
import com.example.myappmobile.presentation.checkout.CheckoutStepHeader
import com.example.myappmobile.presentation.checkout.CheckoutSummaryCard
import com.example.myappmobile.presentation.checkout.CheckoutTopBar
import com.example.myappmobile.presentation.checkout.FloraPrimaryButton

private val paymentOptions = listOf(
    Triple("card", "Card Payment", "Pay securely using your saved or preferred card."),
    Triple("cash_on_delivery", "Cash on Delivery", "Pay in person when your order arrives."),
)

@Composable
fun PaymentScreen(
    onPlaceOrder: () -> Unit = {},
    viewModel: PaymentViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = FloraBeige,
        topBar = { CheckoutTopBar(title = "Payment") },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            CheckoutStepHeader(
                stepLabel = "STEP 3 OF 4",
                title = "Review and complete your purchase",
                subtitle = "Confirm your preferred payment method before placing the order.",
                modifier = Modifier.padding(top = 12.dp),
            )

            CheckoutSummaryCard(
                itemCount = uiState.itemCount,
                subtotal = uiState.subtotal,
                shippingMethod = uiState.shippingMethod,
                shippingCost = uiState.shippingCost,
                total = uiState.total,
            )

            CheckoutSectionCard(
                title = "Payment Method",
                subtitle = "Your selection is sent to the existing checkout API unchanged.",
                icon = Icons.Outlined.Payments,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    paymentOptions.forEach { (value, title, subtitle) ->
                        CheckoutOptionCard(
                            title = title,
                            subtitle = subtitle,
                            icon = if (value == "card") Icons.Outlined.CreditCard else Icons.Outlined.Payments,
                            selected = uiState.paymentMethod == value,
                            onClick = { viewModel.onPaymentMethodSelected(value) },
                        )
                    }
                }
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            FloraPrimaryButton(
                text = "Place Order",
                onClick = { viewModel.placeOrder(onPlaceOrder) },
                enabled = uiState.itemCount > 0 && !uiState.isPlacingOrder,
                isLoading = uiState.isPlacingOrder,
                modifier = Modifier.padding(bottom = 20.dp),
            )
        }
    }
}
