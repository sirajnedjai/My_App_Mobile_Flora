package com.example.myappmobile.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect

private fun floraColorScheme(darkTheme: Boolean) = if (darkTheme) {
    darkColorScheme(
        primary = FloraBrown,
        onPrimary = Black,
        primaryContainer = FloraBrownLight,
        onPrimaryContainer = FloraWhite,
        secondary = Terracotta,
        onSecondary = Black,
        secondaryContainer = FloraCardBg,
        onSecondaryContainer = FloraText,
        background = FloraBeige,
        onBackground = FloraText,
        surface = CreamLight,
        onSurface = FloraText,
        surfaceVariant = FloraCardBg,
        onSurfaceVariant = FloraTextSecondary,
        error = FloraError,
        onError = Black,
        outline = FloraDivider,
        outlineVariant = FloraInputUnderline,
    )
} else {
    lightColorScheme(
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
        outlineVariant = FloraDivider,
    )
}

private fun atelierColorScheme(darkTheme: Boolean) = if (darkTheme) {
    darkColorScheme(
        primary = Terracotta,
        onPrimary = Black,
        primaryContainer = TerracottaLight,
        onPrimaryContainer = CharcoalDark,
        secondary = CharcoalMid,
        onSecondary = Black,
        secondaryContainer = CreamSurface,
        onSecondaryContainer = CharcoalDark,
        tertiary = StoneGray,
        onTertiary = Black,
        background = Cream,
        onBackground = CharcoalDark,
        surface = CreamLight,
        onSurface = CharcoalDark,
        surfaceVariant = CreamDark,
        onSurfaceVariant = CharcoalLight,
        outline = StoneLight,
        outlineVariant = StoneFaint,
        error = ErrorRed,
        onError = Black,
    )
} else {
    lightColorScheme(
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
}

@Composable
fun FloraTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    SideEffect {
        FloraThemeMode.isDarkMode = darkTheme
    }
    MaterialTheme(
        colorScheme = floraColorScheme(darkTheme),
        typography = FloraTypography,
        shapes = FloraShapes,
        content = content,
    )
}

@Composable
fun AtelierTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    SideEffect {
        FloraThemeMode.isDarkMode = darkTheme
    }
    MaterialTheme(
        colorScheme = atelierColorScheme(darkTheme),
        typography = AtelierTypography,
        shapes = AtelierShapes,
        content = content,
    )
}
