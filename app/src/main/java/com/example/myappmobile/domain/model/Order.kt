package com.example.myappmobile.domain.model

data class Order(
    val id: String,
    val reference: String,
    val customerId: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val customerPhone: String = "",
    val customerLocation: String = "",
    val items: List<OrderItem> = emptyList(),
    val status: OrderStatus = OrderStatus.PENDING,
    val total: Double = 0.0,
    val shippingCost: Double = 0.0,
    val tax: Double = 0.0,
    val subtotal: Double = 0.0,
    val shippingMethod: String = "",
    val shippingAddress: Address? = null,
    val placedDate: String = "",
    val estimatedDelivery: String = "",
    val artisanPackaging: String = "Complimentary",
    val imageUrl: String = "",
    val statusHistory: List<OrderStatusEntry> = emptyList(),
)

data class OrderItem(
    val id: String,
    val product: Product,
    val quantity: Int = 1,
    val variant: String = "",
)

enum class OrderStatus {
    PENDING, CONFIRMED, HAND_CRAFTED, SHIPPED, DELIVERED, CANCELLED;

    fun label(): String = when (this) {
        PENDING -> "PENDING"
        CONFIRMED -> "CONFIRMED"
        HAND_CRAFTED -> "HAND-CRAFTED"
        SHIPPED -> "SHIPPED"
        DELIVERED -> "DELIVERED"
        CANCELLED -> "CANCELLED"
    }
}

data class OrderStatusEntry(
    val status: OrderStatus,
    val timestamp: String,
    val note: String = "",
)

data class Address(
    val id: String = "",
    val label: String = "",
    val fullName: String = "",
    val street: String = "",
    val city: String = "",
    val postalCode: String = "",
    val country: String = "",
    val isPrimary: Boolean = false,
)

data class CheckoutDraft(
    val address: Address? = null,
    val shippingMethod: String = "Standard Delivery",
    val paymentMethod: String = "Card ending in 4242",
)
