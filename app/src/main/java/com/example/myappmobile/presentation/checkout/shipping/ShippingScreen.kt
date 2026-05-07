package com.example.myappmobile.presentation.checkout.shipping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.utils.formatPriceDzd
import com.example.myappmobile.presentation.checkout.CheckoutOptionCard
import com.example.myappmobile.presentation.checkout.CheckoutSectionCard
import com.example.myappmobile.presentation.checkout.CheckoutStepHeader
import com.example.myappmobile.presentation.checkout.CheckoutTopBar
import com.example.myappmobile.presentation.checkout.FloraPrimaryButton

private data class ShippingOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val trailingLabel: String,
    val icon: ImageVector,
)

private val shippingOptions = listOf(
    ShippingOption(
        id = "home_delivery",
        title = "Home Delivery",
        subtitle = "Your order is delivered straight to the address you provided.",
        trailingLabel = formatPriceDzd(300.0),
        icon = Icons.Outlined.Home,
    ),
    ShippingOption(
        id = "office_pickup",
        title = "Office Pickup",
        subtitle = "Collect your order from the pickup point when it is ready.",
        trailingLabel = formatPriceDzd(150.0),
        icon = Icons.Outlined.Storefront,
    ),
)

@Composable
fun ShippingScreen(
    onContinue: () -> Unit = {},
    viewModel: ShippingViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = FloraBeige,
        topBar = { CheckoutTopBar(title = "Shipping Method") },
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
                stepLabel = "STEP 2 OF 4",
                title = "Choose a delivery experience",
                subtitle = "Select the option that best fits how you want to receive your FLORA order.",
                modifier = Modifier.padding(top = 12.dp),
            )

            CheckoutSectionCard(
                title = "Available Methods",
                subtitle = "Your shipping preference is saved and used during order creation.",
                icon = Icons.Outlined.LocalShipping,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    shippingOptions.forEach { option ->
                        CheckoutOptionCard(
                            title = option.title,
                            subtitle = option.subtitle,
                            icon = option.icon,
                            selected = uiState.shippingMethod == option.id,
                            onClick = { viewModel.onShippingMethodSelected(option.id) },
                            trailingLabel = option.trailingLabel,
                        )
                    }
                }
            }

            FloraPrimaryButton(
                text = "Continue to Payment",
                onClick = { viewModel.save(onContinue) },
                modifier = Modifier.padding(bottom = 20.dp),
            )
        }
    }
}
