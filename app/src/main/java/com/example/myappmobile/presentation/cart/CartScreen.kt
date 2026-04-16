package com.example.myappmobile.presentation.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import com.example.myappmobile.core.components.CircularIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.components.SmallActionButton
import com.example.myappmobile.core.components.TextActionButton
import com.example.myappmobile.R
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraCardBg
import com.example.myappmobile.core.theme.FloraDivider
import com.example.myappmobile.core.theme.FloraError
import com.example.myappmobile.core.theme.FloraSuccess
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextMuted
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.FloraTheme
import com.example.myappmobile.core.theme.SerifFontFamily
import com.example.myappmobile.core.theme.White
import com.example.myappmobile.domain.model.CartItem
import com.example.myappmobile.domain.model.Product as CartProduct

@Composable
fun CartScreen(
    onBack: () -> Unit = {},
    onCheckout: () -> Unit = {},
    onContinueShopping: () -> Unit = {},
    viewModel: CartViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    CartScreenContent(
        uiState = uiState,
        onBack = onBack,
        onCheckout = onCheckout,
        onContinueShopping = onContinueShopping,
        onIncreaseQuantity = viewModel::onIncreaseQuantity,
        onDecreaseQuantity = viewModel::onDecreaseQuantity,
        onRemoveItem = viewModel::onRemoveItem,
        onPromoCodeChange = viewModel::onPromoCodeChange,
        onApplyPromo = viewModel::onApplyPromoCode,
    )
}

@Composable
private fun CartScreenContent(
    uiState: CartUiState,
    onBack: () -> Unit,
    onCheckout: () -> Unit,
    onContinueShopping: () -> Unit,
    onIncreaseQuantity: (String) -> Unit,
    onDecreaseQuantity: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onPromoCodeChange: (String) -> Unit,
    onApplyPromo: () -> Unit,
) {
    Scaffold(
        containerColor = FloraBeige,
        topBar = {
            CartHeader(
                itemCount = uiState.itemsCount,
                onBack = onBack,
            )
        },
        bottomBar = {
            if (uiState.items.isNotEmpty()) {
                CartSummaryBar(
                    uiState = uiState,
                    onCheckout = onCheckout,
                )
            }
        },
    ) { paddingValues ->
        if (uiState.items.isEmpty()) {
            EmptyCartState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onContinueShopping = onContinueShopping,
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 176.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    CartHeroCard(
                        itemCount = uiState.itemsCount,
                        total = uiState.total,
                    )
                }

                items(uiState.items, key = { it.id }) { item ->
                    CartItemCard(
                        item = item,
                        onIncrease = { onIncreaseQuantity(item.product.id) },
                        onDecrease = { onDecreaseQuantity(item.product.id) },
                        onRemove = { onRemoveItem(item.product.id) },
                    )
                }

                item {
                    PromoCodeCard(
                        promoCode = uiState.promoCode,
                        promoMessage = uiState.promoMessage,
                        promoApplied = uiState.promoApplied,
                        onPromoCodeChange = onPromoCodeChange,
                        onApplyPromo = onApplyPromo,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartHeader(
    itemCount: Int,
    onBack: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.cart_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = SerifFontFamily,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = FloraText,
                )
                Text(
                    text = if (itemCount == 1) {
                        stringResource(R.string.cart_one_piece)
                    } else {
                        stringResource(R.string.cart_piece_count, itemCount)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
            }
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
private fun CartHeroCard(
    itemCount: Int,
    total: Double,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.72f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(FloraCardBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingBag,
                    contentDescription = null,
                    tint = FloraBrown,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.cart_hero_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = FloraText,
                )
                Text(
                    text = if (itemCount == 1) {
                        stringResource(R.string.cart_one_item_checkout)
                    } else {
                        stringResource(R.string.cart_items_checkout, itemCount)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
            }

            Text(
                text = formatCurrency(total),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = FloraBrown,
            )
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.76f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AsyncImage(
                model = item.product.imageUrl,
                contentDescription = item.product.name,
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(FloraCardBg),
                contentScale = ContentScale.Crop,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = FloraText,
                )
                Text(
                    text = item.product.studio,
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
                Text(
                    text = item.product.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextMuted,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatCurrency(item.product.price * item.quantity),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = FloraBrown,
                        modifier = Modifier.weight(1f),
                    )

                    TextActionButton(
                        text = stringResource(R.string.common_remove),
                        onClick = onRemove,
                        leadingIcon = Icons.Outlined.DeleteOutline,
                        accentColor = FloraError,
                    )
                }

                QuantityStepper(
                    quantity = item.quantity,
                    onIncrease = onIncrease,
                    onDecrease = onDecrease,
                    decreaseEnabled = item.quantity > 1,
                )
            }
        }
    }
}

@Composable
private fun QuantityStepper(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    decreaseEnabled: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        QuantityCircleButton(
            icon = Icons.Outlined.Remove,
            enabled = decreaseEnabled,
            onClick = onDecrease,
        )
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = FloraText,
        )
        QuantityCircleButton(
            icon = Icons.Outlined.Add,
            enabled = true,
            onClick = onIncrease,
        )
    }
}

@Composable
private fun QuantityCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    CircularIconButton(
        icon = icon,
        contentDescription = stringResource(R.string.cart_adjust_quantity),
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(34.dp),
    )
}

@Composable
private fun PromoCodeCard(
    promoCode: String,
    promoMessage: String?,
    promoApplied: Boolean,
    onPromoCodeChange: (String) -> Unit,
    onApplyPromo: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.74f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalShipping,
                    contentDescription = null,
                    tint = FloraBrown,
                )
                Column {
                    Text(
                        text = stringResource(R.string.cart_promo_code),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = FloraText,
                    )
                    Text(
                        text = stringResource(R.string.cart_promo_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = FloraTextSecondary,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = promoCode,
                    onValueChange = onPromoCodeChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.cart_enter_code)) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                )
                SmallActionButton(
                    text = stringResource(R.string.common_apply),
                    onClick = onApplyPromo,
                )
            }

            if (!promoMessage.isNullOrBlank()) {
                Text(
                    text = promoMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (promoApplied) FloraSuccess else FloraError,
                )
            }
        }
    }
}

@Composable
private fun CartSummaryBar(
    uiState: CartUiState,
    onCheckout: () -> Unit,
) {
    Surface(
        color = White.copy(alpha = 0.96f),
        shadowElevation = 18.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryRow(label = stringResource(R.string.common_subtotal), value = formatCurrency(uiState.subtotal))
            SummaryRow(label = stringResource(R.string.common_shipping), value = formatCurrency(uiState.shippingFee))
            if (uiState.discount > 0) {
                SummaryRow(
                    label = stringResource(R.string.common_discount),
                    value = "-${formatCurrency(uiState.discount)}",
                    valueColor = FloraSuccess,
                )
            }
            SummaryRow(
                label = stringResource(R.string.common_total),
                value = formatCurrency(uiState.total),
                emphasize = true,
            )
            PrimaryButton(
                text = stringResource(R.string.cart_proceed_checkout),
                onClick = onCheckout,
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    emphasize: Boolean = false,
    valueColor: Color = FloraText,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
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
            text = value,
            style = if (emphasize) {
                MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            } else {
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            },
            color = valueColor,
        )
    }
}

@Composable
private fun EmptyCartState(
    modifier: Modifier = Modifier,
    onContinueShopping: () -> Unit,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.75f))
                .border(1.dp, FloraDivider, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.ShoppingBag,
                contentDescription = null,
                tint = FloraBrown,
                modifier = Modifier.size(42.dp),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.cart_empty_title),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = SerifFontFamily,
                fontStyle = FontStyle.Italic,
            ),
            color = FloraText,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.cart_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = FloraTextSecondary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryButton(
            text = stringResource(R.string.common_continue_shopping),
            onClick = onContinueShopping,
        )
    }
}

private fun formatCurrency(amount: Double): String = "$${"%.2f".format(amount)}"

@Preview(showBackground = true)
@Composable
private fun CartScreenPreview() {
    FloraTheme {
        CartScreenContent(
            uiState = CartUiState(
                items = listOf(
                    CartItem(
                        id = "cart_bag",
                        product = CartProduct(
                            id = "bag",
                            name = "Handwoven Luna Bag",
                            price = 78.0,
                            imageUrl = "https://images.unsplash.com/photo-1584917865442-de89df76afd3",
                            studio = "Atelier Solstice",
                            category = "Accessories",
                        ),
                        quantity = 2,
                    ),
                    CartItem(
                        id = "cart_soap",
                        product = CartProduct(
                            id = "soap",
                            name = "Natural Rose Soap",
                            price = 14.5,
                            imageUrl = "https://images.unsplash.com/photo-1607006483225-4d6c0f3f7fd7",
                            studio = "Maison Fleur",
                            category = "Beauty",
                        ),
                        quantity = 1,
                    ),
                ),
                promoCode = "FLORA10",
                promoApplied = true,
                promoMessage = "FLORA10 applied. You saved 10%.",
            ),
            onBack = {},
            onCheckout = {},
            onContinueShopping = {},
            onIncreaseQuantity = {},
            onDecreaseQuantity = {},
            onRemoveItem = {},
            onPromoCodeChange = {},
            onApplyPromo = {},
        )
    }
}
