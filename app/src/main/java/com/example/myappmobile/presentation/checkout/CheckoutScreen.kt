package com.example.myappmobile.presentation.checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.OutlineButton
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.components.SmallActionButton
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraCardBg
import com.example.myappmobile.core.theme.FloraDivider
import com.example.myappmobile.core.theme.FloraError
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.SerifFontFamily
import com.example.myappmobile.core.theme.White
import com.example.myappmobile.domain.model.CartItem

@Composable
fun CheckoutScreen(
    onBack: () -> Unit = {},
    onPlaceOrderSuccess: () -> Unit = {},
    viewModel: CheckoutViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    CheckoutContent(
        uiState = uiState,
        shippingOptions = viewModel.shippingOptions,
        paymentOptions = viewModel.paymentOptions,
        onBack = onBack,
        onFullNameChange = viewModel::onFullNameChange,
        onStreetChange = viewModel::onStreetChange,
        onCityChange = viewModel::onCityChange,
        onPostalCodeChange = viewModel::onPostalCodeChange,
        onCountryChange = viewModel::onCountryChange,
        onShippingMethodSelected = viewModel::onShippingMethodSelected,
        onPaymentMethodSelected = viewModel::onPaymentMethodSelected,
        onCardNumberChange = viewModel::onCardNumberChange,
        onCardNameChange = viewModel::onCardNameChange,
        onExpiryDateChange = viewModel::onExpiryDateChange,
        onCvvChange = viewModel::onCvvChange,
        onPlaceOrder = { viewModel.placeOrder(onPlaceOrderSuccess) },
    )
}

@Composable
private fun CheckoutContent(
    uiState: CheckoutUiState,
    shippingOptions: List<ShippingOptionUi>,
    paymentOptions: List<PaymentOptionUi>,
    onBack: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onStreetChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onPostalCodeChange: (String) -> Unit,
    onCountryChange: (String) -> Unit,
    onShippingMethodSelected: (String) -> Unit,
    onPaymentMethodSelected: (String) -> Unit,
    onCardNumberChange: (String) -> Unit,
    onCardNameChange: (String) -> Unit,
    onExpiryDateChange: (String) -> Unit,
    onCvvChange: (String) -> Unit,
    onPlaceOrder: () -> Unit,
) {
    Scaffold(
        containerColor = FloraBeige,
        topBar = { CheckoutTopBar(onBack = onBack) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(FloraBeige, White),
                    ),
                )
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                CheckoutHero()
            }

            item {
                CheckoutSectionCard(
                    title = "Shipping Address",
                    icon = Icons.Outlined.LocationOn,
                ) {
                    CheckoutInput(uiState.fullName, onFullNameChange, "Full name")
                    CheckoutInput(uiState.street, onStreetChange, "Street address")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            CheckoutInput(uiState.city, onCityChange, "City")
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            CheckoutInput(uiState.postalCode, onPostalCodeChange, "Postal code")
                        }
                    }
                    CheckoutInput(uiState.country, onCountryChange, "Country")
                }
            }

            item {
                CheckoutSectionCard(
                    title = "Shipping Method",
                    icon = Icons.Outlined.LocalShipping,
                ) {
                    shippingOptions.forEach { option ->
                        SelectableSummaryCard(
                            title = option.title,
                            subtitle = option.subtitle,
                            trailing = option.priceLabel,
                            selected = uiState.shippingMethod == option.title,
                            onClick = { onShippingMethodSelected(option.title) },
                        )
                    }
                }
            }

            item {
                CheckoutSectionCard(
                    title = "Payment",
                    icon = Icons.Outlined.CreditCard,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        paymentOptions.forEach { option ->
                            val selected = uiState.paymentMethod == option.id
                            if (selected) {
                                SmallActionButton(
                                    text = option.title,
                                    onClick = { onPaymentMethodSelected(option.id) },
                                    modifier = Modifier.weight(1f),
                                )
                            } else {
                                OutlineButton(
                                    text = option.title,
                                    onClick = { onPaymentMethodSelected(option.id) },
                                    modifier = Modifier.weight(1f),
                                    fillMaxWidth = false,
                                )
                            }
                        }
                    }

                    if (uiState.paymentMethod == "Card") {
                        CheckoutInput(uiState.cardNumber, onCardNumberChange, "Card number")
                        CheckoutInput(uiState.cardName, onCardNameChange, "Cardholder name")
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                CheckoutInput(uiState.expiryDate, onExpiryDateChange, "Expiry")
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                CheckoutInput(uiState.cvv, onCvvChange, "CVV")
                            }
                        }
                    }
                }
            }

            item {
                OrderSummaryCard(
                    uiState = uiState,
                    onPlaceOrder = onPlaceOrder,
                )
            }

            item {
                Text(
                    text = "FLORA Atelier checkout keeps every detail quiet, secure, and intentional.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutTopBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "FLORA",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = SerifFontFamily),
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
private fun CheckoutHero() {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(White, FloraCardBg),
                    ),
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Checkout",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = SerifFontFamily,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = FloraText,
            )
            Text(
                text = "Complete your order with artisan delivery details and a refined final review.",
                style = MaterialTheme.typography.bodyMedium,
                color = FloraTextSecondary,
            )
        }
    }
}

@Composable
private fun CheckoutSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = FloraBrown)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
                content()
            },
        )
    }
}

@Composable
private fun CheckoutInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = White,
            unfocusedContainerColor = White,
            focusedBorderColor = FloraBrown,
            unfocusedBorderColor = FloraDivider,
        ),
    )
}

@Composable
private fun SelectableSummaryCard(
    title: String,
    subtitle: String,
    trailing: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = if (selected) FloraCardBg else White,
        border = BorderStroke(1.dp, if (selected) FloraBrown else FloraDivider),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = FloraTextSecondary)
            }
            Text(trailing, style = MaterialTheme.typography.titleMedium, color = FloraBrown)
        }
    }
}

@Composable
private fun OrderSummaryCard(
    uiState: CheckoutUiState,
    onPlaceOrder: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ShoppingBag, contentDescription = null, tint = FloraBrown)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Order Summary",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                )
            }

            if (uiState.items.isEmpty()) {
                Text(
                    text = "Your cart is empty. Add a product before placing the order.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FloraTextSecondary,
                )
            } else {
                uiState.items.forEach { item ->
                    CheckoutItemRow(item = item)
                }
            }

            HorizontalDivider(color = FloraDivider)
            PriceLine("Subtotal", uiState.subtotal)
            PriceLine("Shipping", uiState.shippingCost)
            PriceLine("Estimated tax", uiState.tax)
            HorizontalDivider(color = FloraDivider)
            PriceLine("Total", uiState.total, emphasize = true)

            uiState.errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraError,
                )
            }

            PrimaryButton(
                text = if (uiState.isPlacingOrder) "Placing Order..." else "Place Order",
                onClick = onPlaceOrder,
                enabled = uiState.canPlaceOrder,
                isLoading = uiState.isPlacingOrder,
                leadingIcon = Icons.Outlined.Lock,
            )
        }
    }
}

@Composable
private fun CheckoutItemRow(item: CartItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AsyncImage(
            model = item.product.imageUrl,
            contentDescription = item.product.name,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Crop,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.product.name, style = MaterialTheme.typography.titleMedium, color = FloraText)
            Text(
                text = item.selectedVariant.ifBlank { item.product.category },
                style = MaterialTheme.typography.bodySmall,
                color = FloraTextSecondary,
            )
            Text(
                text = "Qty ${item.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = FloraTextSecondary,
            )
        }
        Text(
            text = "$${"%.2f".format(item.product.price * item.quantity)}",
            style = MaterialTheme.typography.titleMedium,
            color = FloraText,
        )
    }
}

@Composable
private fun PriceLine(
    label: String,
    amount: Double,
    emphasize: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = if (emphasize) {
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = FloraTextSecondary,
        )
        Text(
            text = "$${"%.2f".format(amount)}",
            style = if (emphasize) {
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = FloraText,
        )
    }
}
