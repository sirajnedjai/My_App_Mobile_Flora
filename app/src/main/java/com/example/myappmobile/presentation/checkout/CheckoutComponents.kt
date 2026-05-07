package com.example.myappmobile.presentation.checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraBrownLight
import com.example.myappmobile.core.theme.FloraCardBg
import com.example.myappmobile.core.theme.FloraDivider
import com.example.myappmobile.core.theme.FloraSelectedCard
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.FloraWhite
import com.example.myappmobile.core.theme.SerifFontFamily
import com.example.myappmobile.core.utils.formatPriceDzd

@Composable
fun FloraPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    PrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        leadingIcon = leadingIcon,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontStyle = FontStyle.Italic),
                color = FloraText,
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Go back",
                        tint = FloraText,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = FloraBeige,
            titleContentColor = FloraText,
            navigationIconContentColor = FloraText,
        ),
    )
}

@Composable
fun CheckoutStepHeader(
    stepLabel: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = FloraWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            FloraWhite,
                            FloraCardBg.copy(alpha = 0.92f),
                        ),
                    ),
                )
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                color = FloraBrown.copy(alpha = 0.12f),
                shape = CircleShape,
            ) {
                Text(
                    text = stepLabel,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = FloraBrown,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = SerifFontFamily),
                color = FloraText,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = FloraTextSecondary,
            )
        }
    }
}

@Composable
fun CheckoutSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = FloraWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            content = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(FloraBrown.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = FloraBrown,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = FloraText,
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = FloraTextSecondary,
                        )
                    }
                }
                content()
            },
        )
    }
}

@Composable
fun CheckoutTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = FloraText),
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = label,
            )
        },
        singleLine = singleLine,
        shape = MaterialTheme.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FloraBrown,
            unfocusedBorderColor = FloraDivider,
            focusedLabelColor = FloraBrown,
            unfocusedLabelColor = FloraTextSecondary,
            cursorColor = FloraBrown,
            focusedLeadingIconColor = FloraBrown,
            unfocusedLeadingIconColor = FloraTextSecondary,
            focusedContainerColor = FloraWhite,
            unfocusedContainerColor = FloraWhite,
        ),
    )
}

@Composable
fun CheckoutOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingLabel: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val containerColor = if (selected) FloraBrown.copy(alpha = 0.08f) else FloraSelectedCard.copy(alpha = 0.72f)
    val borderColor = if (selected) FloraBrown else FloraDivider.copy(alpha = 0.95f)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(width = if (selected) 1.5.dp else 1.dp, color = borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 3.dp else 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        if (selected) FloraBrown.copy(alpha = 0.14f) else FloraBrownLight.copy(alpha = 0.10f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (selected) FloraBrown else FloraTextSecondary,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = FloraText,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
            }
            if (!trailingLabel.isNullOrBlank()) {
                Text(
                    text = trailingLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = FloraBrown,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            RadioButton(
                selected = selected,
                onClick = null,
            )
        }
    }
}

@Composable
fun CheckoutSummaryCard(
    modifier: Modifier = Modifier,
    itemCount: Int,
    subtotal: Double,
    shippingMethod: String,
    shippingCost: Double? = null,
    total: Double? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = FloraWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleLarge,
                color = FloraText,
            )
            SummaryLine(label = "Items", value = itemCount.toString())
            SummaryLine(label = "Subtotal", value = formatCurrency(subtotal))
            SummaryLine(label = "Shipping", value = formatShippingMethod(shippingMethod))
            if (shippingCost != null) {
                SummaryLine(label = "Delivery Fee", value = formatCurrency(shippingCost))
            }
            total?.let {
                HorizontalDivider(color = FloraDivider.copy(alpha = 0.75f))
                SummaryLine(
                    label = "Total",
                    value = formatCurrency(it),
                    emphasized = true,
                )
            }
        }
    }
}

@Composable
fun SuccessInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = FloraTextSecondary,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            modifier = Modifier.weight(1.2f),
            style = MaterialTheme.typography.titleMedium,
            color = FloraText,
        )
    }
}

@Composable
private fun SummaryLine(
    label: String,
    value: String,
    emphasized: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (emphasized) FloraText else FloraTextSecondary,
        )
        Text(
            text = value,
            style = if (emphasized) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            color = if (emphasized) FloraBrown else FloraText,
        )
    }
}

fun formatShippingMethod(value: String): String = when (value) {
    "home_delivery" -> "Home Delivery"
    "office_pickup" -> "Office Pickup"
    else -> value
        .split("_", "-", " ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { char -> char.titlecase() }
        }
        .ifBlank { "Not provided" }
}

fun formatPaymentMethod(value: String): String = when (value) {
    "cash_on_delivery" -> "Cash on Delivery"
    "card" -> "Card Payment"
    else -> value
        .split("_", "-", " ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { char -> char.titlecase() }
        }
        .ifBlank { "Not provided" }
}

fun formatCurrency(value: Double): String = formatPriceDzd(value)

fun buildAddressSummary(
    state: String,
    municipality: String,
    neighborhood: String,
    streetAddress: String,
    postalCode: String,
    country: String,
): String = listOf(
    streetAddress,
    neighborhood,
    municipality,
    state,
    postalCode,
    country,
).filter { it.isNotBlank() }.joinToString(", ")
