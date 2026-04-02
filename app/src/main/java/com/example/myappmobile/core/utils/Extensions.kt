package com.example.myappmobile.core.utils

import com.example.myappmobile.domain.model.OrderStatus
import com.example.myappmobile.core.theme.*
import androidx.compose.ui.graphics.Color

fun Double.formatPrice(): String = "$%.2f".format(this)

fun Int.formatPrice(): String = "$$this"

fun OrderStatus.statusColor(): Color = when (this) {
    OrderStatus.PENDING -> StatusAmber
    OrderStatus.CONFIRMED -> StatusBlue
    OrderStatus.HAND_CRAFTED -> StatusGreen
    OrderStatus.SHIPPED -> StatusBlue
    OrderStatus.DELIVERED -> StatusGreen
    OrderStatus.CANCELLED -> StatusRed
}

fun OrderStatus.statusBgColor(): Color = when (this) {
    OrderStatus.PENDING -> StatusAmberLight
    OrderStatus.CONFIRMED -> StatusBlueLight
    OrderStatus.HAND_CRAFTED -> StatusGreenLight
    OrderStatus.SHIPPED -> StatusBlueLight
    OrderStatus.DELIVERED -> StatusGreenLight
    OrderStatus.CANCELLED -> StatusRedLight
}