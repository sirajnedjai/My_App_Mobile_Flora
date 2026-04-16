package com.example.myappmobile.core.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object FloraThemeMode {
    var isDarkMode: Boolean by mutableStateOf(false)
}

private object Palette {
    const val lightCream = 0xFFF5F0E8
    const val darkCream = 0xFF151311
    const val lightCreamLight = 0xFFFAF7F2
    const val darkCreamLight = 0xFF1B1815
    const val lightCreamDark = 0xFFEDE6D6
    const val darkCreamDark = 0xFF24201C
    const val lightCreamSurface = 0xFFF0EBE0
    const val darkCreamSurface = 0xFF211D19

    const val lightCharcoalDark = 0xFF1C1C1A
    const val darkCharcoalDark = 0xFFF4EEE7
    const val lightCharcoalMid = 0xFF3A3A37
    const val darkCharcoalMid = 0xFFD0C7BD
    const val lightCharcoalLight = 0xFF6B6B66
    const val darkCharcoalLight = 0xFFA59688

    const val lightStoneGray = 0xFF9A9A94
    const val darkStoneGray = 0xFF908274
    const val lightStoneLight = 0xFFCECEC8
    const val darkStoneLight = 0xFF443B33
    const val lightStoneFaint = 0xFFE8E5DE
    const val darkStoneFaint = 0xFF2B2622

    const val white = 0xFFFFFFFF
    const val black = 0xFF0D0D0B

    const val lightTerracotta = 0xFFC4856A
    const val darkTerracotta = 0xFFD79F87
    const val lightTerracottaLight = 0xFFD4A090
    const val darkTerracottaLight = 0xFF8C624E
    const val lightTerracottaDark = 0xFFA86B52
    const val darkTerracottaDark = 0xFFE6B49A
    const val lightPriceGreen = 0xFF4A7C59
    const val darkPriceGreen = 0xFF76C18C
    const val lightErrorRed = 0xFFB5442A
    const val darkErrorRed = 0xFFE77B5F
    const val starGold = 0xFFD4A843
    const val lightTrendGreen = 0xFF3D8A5A
    const val darkTrendGreen = 0xFF66BC86

    const val lightFloraRed = 0xFF8B3A2A
    const val darkFloraRed = 0xFFB56C5E
    const val lightFloraRedLight = 0xFFA85548
    const val darkFloraRedLight = 0xFF8F5F55
    const val lightFloraRedDark = 0xFF6B2A1C
    const val darkFloraRedDark = 0xFFE9B3A8

    const val lightFloraBeige = 0xFFF5F1ED
    const val darkFloraBeige = 0xFF141110
    const val lightFloraBeigeLight = 0xFFF0EBE4
    const val darkFloraBeigeLight = 0xFF1C1816
    const val lightFloraBrown = 0xFF7D4F3A
    const val darkFloraBrown = 0xFFD3AE9A
    const val lightFloraBrownLight = 0xFFA07060
    const val darkFloraBrownLight = 0xFF8B6557
    const val lightFloraBrownDark = 0xFF5C3526
    const val darkFloraBrownDark = 0xFFF0D2C2
    const val lightFloraText = 0xFF2C2C2C
    const val darkFloraText = 0xFFF6F0EA
    const val lightFloraTextSecondary = 0xFF6B6B6B
    const val darkFloraTextSecondary = 0xFFB7A99D
    const val lightFloraTextMuted = 0xFFAAAAAA
    const val darkFloraTextMuted = 0xFF7F7267
    const val lightFloraCardBg = 0xFFEFEBE5
    const val darkFloraCardBg = 0xFF211C1A
    const val lightSearchGradientTop = 0xFFF7F3EF
    const val darkSearchGradientTop = 0xFF181412
    const val lightSearchGradientBottom = 0xFFF1EAE4
    const val darkSearchGradientBottom = 0xFF211B17
    const val lightSearchContainer = 0xFFEDE6DF
    const val darkSearchContainer = 0xFF2A241F
    const val lightSearchField = 0xFFE9E2DB
    const val darkSearchField = 0xFF342C27
    const val lightSearchChipInactive = 0xFFF2ECE6
    const val darkSearchChipInactive = 0xFF312A25
    const val lightSearchChipText = 0xFF6E6E6E
    const val darkSearchChipText = 0xFFC0B2A6
    const val lightSearchButtonStart = 0xFF8B5E3C
    const val darkSearchButtonStart = 0xFFB98A67
    const val lightSearchButtonEnd = 0xFFC08A6E
    const val darkSearchButtonEnd = 0xFFD6A68D
    const val lightFloraDivider = 0xFFD8D0C8
    const val darkFloraDivider = 0xFF3C342E
    const val lightFloraInputUnderline = 0xFFB8AEA4
    const val darkFloraInputUnderline = 0xFF5A4D43
    const val lightFloraError = 0xFFC0392B
    const val darkFloraError = 0xFFE88375
    const val lightFloraSuccess = 0xFF27AE60
    const val darkFloraSuccess = 0xFF67C98E

    const val lightStatusGreen = 0xFF4A7C59
    const val darkStatusGreen = 0xFF6EC18B
    const val lightStatusGreenLight = 0xFFEBF5EE
    const val darkStatusGreenLight = 0xFF183323
    const val lightStatusAmber = 0xFFB5813A
    const val darkStatusAmber = 0xFFE3B46C
    const val lightStatusAmberLight = 0xFFFFF3E0
    const val darkStatusAmberLight = 0xFF3A2A15
    const val lightStatusBlue = 0xFF3A6B8A
    const val darkStatusBlue = 0xFF77B3DA
    const val lightStatusBlueLight = 0xFFE3F0F8
    const val darkStatusBlueLight = 0xFF152937
    const val lightStatusRed = 0xFFB5442A
    const val darkStatusRed = 0xFFE67D62
    const val lightStatusRedLight = 0xFFFDEDE8
    const val darkStatusRedLight = 0xFF341815

    const val floraGoogleRed = 0xFFEA4335
    const val floraGoogleBlue = 0xFF4285F4
    const val floraGoogleYellow = 0xFFFBBC05
    const val floraGoogleGreen = 0xFF34A853
}

private fun themeColor(light: Long, dark: Long): Color =
    Color(if (FloraThemeMode.isDarkMode) dark else light)

val Cream: Color get() = themeColor(Palette.lightCream, Palette.darkCream)
val CreamLight: Color get() = themeColor(Palette.lightCreamLight, Palette.darkCreamLight)
val CreamDark: Color get() = themeColor(Palette.lightCreamDark, Palette.darkCreamDark)
val CreamSurface: Color get() = themeColor(Palette.lightCreamSurface, Palette.darkCreamSurface)

val CharcoalDark: Color get() = themeColor(Palette.lightCharcoalDark, Palette.darkCharcoalDark)
val CharcoalMid: Color get() = themeColor(Palette.lightCharcoalMid, Palette.darkCharcoalMid)
val CharcoalLight: Color get() = themeColor(Palette.lightCharcoalLight, Palette.darkCharcoalLight)

val StoneGray: Color get() = themeColor(Palette.lightStoneGray, Palette.darkStoneGray)
val StoneLight: Color get() = themeColor(Palette.lightStoneLight, Palette.darkStoneLight)
val StoneFaint: Color get() = themeColor(Palette.lightStoneFaint, Palette.darkStoneFaint)

val White = Color(Palette.white)
val Black = Color(Palette.black)

val Terracotta: Color get() = themeColor(Palette.lightTerracotta, Palette.darkTerracotta)
val TerracottaLight: Color get() = themeColor(Palette.lightTerracottaLight, Palette.darkTerracottaLight)
val TerracottaDark: Color get() = themeColor(Palette.lightTerracottaDark, Palette.darkTerracottaDark)
val PriceGreen: Color get() = themeColor(Palette.lightPriceGreen, Palette.darkPriceGreen)
val ErrorRed: Color get() = themeColor(Palette.lightErrorRed, Palette.darkErrorRed)
val StarGold = Color(Palette.starGold)
val TrendGreen: Color get() = themeColor(Palette.lightTrendGreen, Palette.darkTrendGreen)
val CardWhite: Color get() = if (FloraThemeMode.isDarkMode) CreamLight else White

val FloraRed: Color get() = themeColor(Palette.lightFloraRed, Palette.darkFloraRed)
val FloraRedLight: Color get() = themeColor(Palette.lightFloraRedLight, Palette.darkFloraRedLight)
val FloraRedDark: Color get() = themeColor(Palette.lightFloraRedDark, Palette.darkFloraRedDark)

val FloraBeige: Color get() = themeColor(Palette.lightFloraBeige, Palette.darkFloraBeige)
val FloraBeigeLight: Color get() = themeColor(Palette.lightFloraBeigeLight, Palette.darkFloraBeigeLight)
val FloraBrown: Color get() = themeColor(Palette.lightFloraBrown, Palette.darkFloraBrown)
val FloraBrownLight: Color get() = themeColor(Palette.lightFloraBrownLight, Palette.darkFloraBrownLight)
val FloraBrownDark: Color get() = themeColor(Palette.lightFloraBrownDark, Palette.darkFloraBrownDark)
val FloraText: Color get() = themeColor(Palette.lightFloraText, Palette.darkFloraText)
val FloraTextSecondary: Color get() = themeColor(Palette.lightFloraTextSecondary, Palette.darkFloraTextSecondary)
val FloraTextMuted: Color get() = themeColor(Palette.lightFloraTextMuted, Palette.darkFloraTextMuted)
val FloraWhite: Color get() = if (FloraThemeMode.isDarkMode) CreamLight else White
val FloraCardBg: Color get() = themeColor(Palette.lightFloraCardBg, Palette.darkFloraCardBg)
val SearchGradientTop: Color get() = themeColor(Palette.lightSearchGradientTop, Palette.darkSearchGradientTop)
val SearchGradientBottom: Color get() = themeColor(Palette.lightSearchGradientBottom, Palette.darkSearchGradientBottom)
val SearchContainer: Color get() = themeColor(Palette.lightSearchContainer, Palette.darkSearchContainer)
val SearchField: Color get() = themeColor(Palette.lightSearchField, Palette.darkSearchField)
val SearchChipInactive: Color get() = themeColor(Palette.lightSearchChipInactive, Palette.darkSearchChipInactive)
val SearchChipText: Color get() = themeColor(Palette.lightSearchChipText, Palette.darkSearchChipText)
val SearchButtonStart: Color get() = themeColor(Palette.lightSearchButtonStart, Palette.darkSearchButtonStart)
val SearchButtonEnd: Color get() = themeColor(Palette.lightSearchButtonEnd, Palette.darkSearchButtonEnd)
val FloraSelectedCard: Color get() = if (FloraThemeMode.isDarkMode) CreamSurface else White
val FloraDivider: Color get() = themeColor(Palette.lightFloraDivider, Palette.darkFloraDivider)
val FloraInputUnderline: Color get() = themeColor(Palette.lightFloraInputUnderline, Palette.darkFloraInputUnderline)
val FloraError: Color get() = themeColor(Palette.lightFloraError, Palette.darkFloraError)
val FloraSuccess: Color get() = themeColor(Palette.lightFloraSuccess, Palette.darkFloraSuccess)

val StatusGreen: Color get() = themeColor(Palette.lightStatusGreen, Palette.darkStatusGreen)
val StatusGreenLight: Color get() = themeColor(Palette.lightStatusGreenLight, Palette.darkStatusGreenLight)
val StatusAmber: Color get() = themeColor(Palette.lightStatusAmber, Palette.darkStatusAmber)
val StatusAmberLight: Color get() = themeColor(Palette.lightStatusAmberLight, Palette.darkStatusAmberLight)
val StatusBlue: Color get() = themeColor(Palette.lightStatusBlue, Palette.darkStatusBlue)
val StatusBlueLight: Color get() = themeColor(Palette.lightStatusBlueLight, Palette.darkStatusBlueLight)
val StatusRed: Color get() = themeColor(Palette.lightStatusRed, Palette.darkStatusRed)
val StatusRedLight: Color get() = themeColor(Palette.lightStatusRedLight, Palette.darkStatusRedLight)

val FloraAppleIcon: Color get() = if (FloraThemeMode.isDarkMode) White else Black
val FloraGoogleRed = Color(Palette.floraGoogleRed)
val FloraGoogleBlue = Color(Palette.floraGoogleBlue)
val FloraGoogleYellow = Color(Palette.floraGoogleYellow)
val FloraGoogleGreen = Color(Palette.floraGoogleGreen)
