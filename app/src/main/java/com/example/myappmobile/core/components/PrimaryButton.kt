package com.example.myappmobile.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraBrownLight
import com.example.myappmobile.core.theme.FloraCardBg
import com.example.myappmobile.core.theme.FloraDivider
import com.example.myappmobile.core.theme.FloraError
import com.example.myappmobile.core.theme.FloraSelectedCard
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextMuted
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.FloraWhite

private object FloraButtonTokens {
    val fullWidthMinHeight = 56.dp
    val compactMinHeight = 42.dp
    val buttonShape
        @Composable get() = MaterialTheme.shapes.extraLarge
    val smallShape
        @Composable get() = MaterialTheme.shapes.large
    val iconContainerSize = 42.dp
    val iconSize = 18.dp
    val buttonPadding = PaddingValues(horizontal = 22.dp, vertical = 16.dp)
    val compactPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    fillMaxWidth: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    BaseFloraButton(
        text = text,
        onClick = onClick,
        modifier = if (fillMaxWidth) modifier.fillMaxWidth() else modifier,
        isLoading = isLoading,
        enabled = enabled,
        leadingIcon = leadingIcon,
        colors = ButtonDefaults.buttonColors(
            containerColor = FloraBrown,
            contentColor = FloraWhite,
            disabledContainerColor = FloraBrownLight.copy(alpha = 0.55f),
            disabledContentColor = FloraWhite.copy(alpha = 0.68f),
        ),
        border = null,
        labelColor = FloraWhite,
    )
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fillMaxWidth: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    BaseTonalButton(
        text = text,
        onClick = onClick,
        modifier = if (fillMaxWidth) modifier.fillMaxWidth() else modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        containerColor = FloraCardBg,
        contentColor = FloraText,
    )
}

@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fillMaxWidth: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = if (fillMaxWidth) modifier.fillMaxWidth() else modifier
            .defaultMinSize(minHeight = FloraButtonTokens.fullWidthMinHeight),
        enabled = enabled,
        shape = FloraButtonTokens.buttonShape,
        border = BorderStroke(1.dp, if (enabled) FloraDivider else FloraDivider.copy(alpha = 0.55f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = FloraSelectedCard.copy(alpha = 0.96f),
            contentColor = FloraText,
            disabledContainerColor = FloraSelectedCard.copy(alpha = 0.55f),
            disabledContentColor = FloraTextMuted,
        ),
        contentPadding = FloraButtonTokens.buttonPadding,
    ) {
        FloraButtonLabel(text = text, icon = leadingIcon, contentColor = FloraText)
    }
}

@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fillMaxWidth: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    BaseFloraButton(
        text = text,
        onClick = onClick,
        modifier = if (fillMaxWidth) modifier.fillMaxWidth() else modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        colors = ButtonDefaults.buttonColors(
            containerColor = FloraError,
            contentColor = FloraWhite,
            disabledContainerColor = FloraError.copy(alpha = 0.45f),
            disabledContentColor = FloraWhite.copy(alpha = 0.68f),
        ),
        border = null,
        labelColor = FloraWhite,
    )
}

@Composable
fun TextActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    accentColor: Color = FloraBrown,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = FloraButtonTokens.compactMinHeight),
        enabled = enabled,
        shape = FloraButtonTokens.smallShape,
        colors = ButtonDefaults.textButtonColors(
            contentColor = accentColor,
            disabledContentColor = accentColor.copy(alpha = 0.42f),
        ),
        contentPadding = FloraButtonTokens.compactPadding,
    ) {
        FloraButtonLabel(text = text, icon = leadingIcon, contentColor = accentColor, compact = true)
    }
}

@Composable
fun SmallActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    danger: Boolean = false,
) {
    if (danger) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.defaultMinSize(minHeight = FloraButtonTokens.compactMinHeight),
            enabled = enabled,
            shape = FloraButtonTokens.smallShape,
            border = BorderStroke(1.dp, FloraError.copy(alpha = if (enabled) 0.36f else 0.18f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = FloraSelectedCard.copy(alpha = 0.94f),
                contentColor = FloraError,
                disabledContentColor = FloraError.copy(alpha = 0.45f),
                disabledContainerColor = FloraSelectedCard.copy(alpha = 0.55f),
            ),
            contentPadding = FloraButtonTokens.compactPadding,
        ) {
            FloraButtonLabel(text = text, icon = leadingIcon, contentColor = FloraError, compact = true)
        }
    } else {
        BaseTonalButton(
            text = text,
            onClick = onClick,
            modifier = modifier.defaultMinSize(minHeight = FloraButtonTokens.compactMinHeight),
            enabled = enabled,
            leadingIcon = leadingIcon,
            containerColor = FloraCardBg,
            contentColor = FloraText,
            compact = true,
        )
    }
}

@Composable
fun CircularIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    filled: Boolean = false,
    danger: Boolean = false,
) {
    val containerColor = when {
        danger -> FloraError.copy(alpha = 0.12f)
        filled -> FloraBrown
        else -> FloraSelectedCard.copy(alpha = 0.92f)
    }
    val contentColor = when {
        danger -> FloraError
        filled -> FloraWhite
        else -> FloraText
    }

    IconButton(
        onClick = onClick,
        modifier = modifier.size(FloraButtonTokens.iconContainerSize),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.45f),
            disabledContentColor = contentColor.copy(alpha = 0.42f),
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(FloraButtonTokens.iconSize),
        )
    }
}

@Composable
private fun BaseFloraButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    colors: ButtonColors,
    border: BorderStroke?,
    labelColor: Color,
) {
    Button(
        onClick = { if (!isLoading) onClick() },
        modifier = modifier.defaultMinSize(minHeight = FloraButtonTokens.fullWidthMinHeight),
        enabled = enabled && !isLoading,
        shape = FloraButtonTokens.buttonShape,
        colors = colors,
        border = border,
        contentPadding = FloraButtonTokens.buttonPadding,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 1.dp,
            disabledElevation = 0.dp,
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = labelColor,
                    strokeWidth = 2.dp,
                )
            } else {
                FloraButtonLabel(text = text, icon = leadingIcon, contentColor = labelColor)
            }
        }
    }
}

@Composable
private fun BaseTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    leadingIcon: ImageVector? = null,
    containerColor: Color,
    contentColor: Color,
    compact: Boolean = false,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(
            minHeight = if (compact) FloraButtonTokens.compactMinHeight else FloraButtonTokens.fullWidthMinHeight,
        ),
        enabled = enabled,
        shape = if (compact) FloraButtonTokens.smallShape else FloraButtonTokens.buttonShape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.45f),
            disabledContentColor = contentColor.copy(alpha = 0.42f),
        ),
        contentPadding = if (compact) FloraButtonTokens.compactPadding else FloraButtonTokens.buttonPadding,
    ) {
        FloraButtonLabel(text = text, icon = leadingIcon, contentColor = contentColor, compact = compact)
    }
}

@Composable
private fun FloraButtonLabel(
    text: String,
    icon: ImageVector? = null,
    contentColor: Color,
    compact: Boolean = false,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(if (compact) 16.dp else 18.dp),
            )
        }
        if (icon != null) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = if (compact) {
                MaterialTheme.typography.titleSmall.copy(color = contentColor)
            } else {
                MaterialTheme.typography.labelLarge.copy(color = contentColor)
            },
        )
    }
}
