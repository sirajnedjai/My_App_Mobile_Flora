package com.example.myappmobile.core.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

private const val USD_TO_DZD_RATE = 135.0
private const val BACKEND_PRICES_ARE_USD = true

private val currencySymbols = DecimalFormatSymbols(Locale.US)
private val integerCurrencyFormat = DecimalFormat("#,##0", currencySymbols)
private val decimalCurrencyFormat = DecimalFormat("#,##0.##", currencySymbols)

fun formatPriceDzd(amount: Double): String {
    val displayAmount = convertPriceForDisplay(amount)
    val formatted = if (displayAmount.hasVisibleDecimals()) {
        decimalCurrencyFormat.format(displayAmount)
    } else {
        integerCurrencyFormat.format(displayAmount)
    }
    return "$formatted DA"
}

fun formatCompactPriceDzd(amount: Double): String {
    val displayAmount = convertPriceForDisplay(amount)
    val absolute = abs(displayAmount)
    val compact = when {
        absolute >= 1_000_000 -> decimalCurrencyFormat.format(displayAmount / 1_000_000) + "M"
        absolute >= 1_000 -> decimalCurrencyFormat.format(displayAmount / 1_000) + "K"
        displayAmount.hasVisibleDecimals() -> decimalCurrencyFormat.format(displayAmount)
        else -> integerCurrencyFormat.format(displayAmount)
    }
    return "$compact DA"
}

fun formatRawPriceInputPreview(rawValue: String): String {
    val parsed = rawValue.toDoubleOrNull() ?: 0.0
    return formatPriceDzd(parsed)
}

fun convertPriceForDisplay(amount: Double): Double =
    if (BACKEND_PRICES_ARE_USD) amount * USD_TO_DZD_RATE else amount

private fun Double.hasVisibleDecimals(): Boolean = abs(this - this.toLong()) > 0.000001
