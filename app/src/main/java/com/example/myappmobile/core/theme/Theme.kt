package com.example.myappmobile.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val FloraColorScheme = lightColorScheme(
    primary = FloraBrown,
    onPrimary = FloraWhite,
    primaryContainer = FloraBrownLight,
    onPrimaryContainer = FloraWhite,
    secondary = FloraBrownLight,
    onSecondary = FloraWhite,
    background = FloraBeige,
    onBackground = FloraText,
    surface = FloraWhite,
    onSurface = FloraText,
    surfaceVariant = FloraCardBg,
    onSurfaceVariant = FloraTextSecondary,
    error = FloraError,
    onError = FloraWhite,
    outline = FloraInputUnderline,
)

private val AtelierColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = White,
    primaryContainer = TerracottaLight,
    onPrimaryContainer = CharcoalDark,
    secondary = CharcoalMid,
    onSecondary = White,
    secondaryContainer = StoneFaint,
    onSecondaryContainer = CharcoalDark,
    tertiary = StoneGray,
    onTertiary = White,
    background = Cream,
    onBackground = CharcoalDark,
    surface = CreamLight,
    onSurface = CharcoalDark,
    surfaceVariant = CreamDark,
    onSurfaceVariant = CharcoalLight,
    outline = StoneLight,
    outlineVariant = StoneFaint,
    error = ErrorRed,
    onError = White,
)

@Composable
fun FloraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FloraColorScheme,
        typography = FloraTypography,
        shapes = FloraShapes,
        content = content,
    )
}

@Composable
fun AtelierTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AtelierColorScheme,
        typography = AtelierTypography,
        shapes = AtelierShapes,
        content = content,
    )
}
