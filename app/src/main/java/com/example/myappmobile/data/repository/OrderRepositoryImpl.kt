package com.example.myappmobile.data.repository

import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.local.dummy.DummyOrders
import com.example.myappmobile.domain.model.Address
import com.example.myappmobile.domain.model.CheckoutDraft
import com.example.myappmobile.domain.model.Order
import com.example.myappmobile.domain.model.OrderItem
import com.example.myappmobile.domain.model.OrderStatus
import com.example.myappmobile.domain.model.OrderStatusEntry
import com.example.myappmobile.domain.repository.CartRepository
import com.example.myappmobile.domain.repository.OrderRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class OrderRepositoryImpl(
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepositoryImpl,
) : OrderRepository {

    private val timestampFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm")

    private val _checkoutDraft = MutableStateFlow(
        CheckoutDraft(address = DummyOrders.address1)
    )
    override val checkoutDraft: StateFlow<CheckoutDraft> = _checkoutDraft.asStateFlow()

    private val _orders = MutableStateFlow(
        DummyOrders.recentOrders.map(::withSeededHistory)
    )
    override val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private var latestOrder: Order? = DummyOrders.confirmationOrder

    override suspend fun updateAddress(address: Address) {
        _checkoutDraft.update { it.copy(address = address) }
    }

    override suspend fun updateShippingMethod(method: String) {
        _checkoutDraft.update { it.copy(shippingMethod = method) }
    }

    override suspend fun updatePaymentMethod(method: String) {
        _checkoutDraft.update { it.copy(paymentMethod = method) }
    }

    override suspend fun createOrder(): Order {
        val currentUser = authRepository.currentUser.value
        val items = cartRepository.getCheckoutItems()
        val subtotal = items.sumOf { it.product.price * it.quantity }
        val shippingCost = if (_checkoutDraft.value.shippingMethod.contains("Concierge")) 25.0 else 12.0
        val tax = subtotal * 0.08
        val placedAt = LocalDateTime.now()
        val placedDate = placedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))

        val order = Order(
            id = "order_${System.currentTimeMillis()}",
            reference = "ATL-${(100000..999999).random()}",
            customerId = currentUser.id,
            customerName = _checkoutDraft.value.address?.fullName.orEmpty().ifBlank { currentUser.fullName },
            customerEmail = currentUser.email,
            customerPhone = currentUser.phone,
            customerLocation = listOfNotNull(
                _checkoutDraft.value.address?.city?.takeIf { it.isNotBlank() },
                _checkoutDraft.value.address?.country?.takeIf { it.isNotBlank() },
            ).joinToString(", "),
            items = items.map { cartItem ->
                OrderItem(
                    id = cartItem.id,
                    product = cartItem.product,
                    quantity = cartItem.quantity,
                )
            },
            status = OrderStatus.CONFIRMED,
            subtotal = subtotal,
            shippingCost = shippingCost,
            tax = tax,
            total = subtotal + shippingCost + tax,
            shippingMethod = _checkoutDraft.value.shippingMethod,
            shippingAddress = _checkoutDraft.value.address,
            placedDate = placedDate,
            estimatedDelivery = "3-5 business days",
            statusHistory = listOf(
                OrderStatusEntry(
                    status = OrderStatus.CONFIRMED,
                    timestamp = formatTimestamp(placedAt),
                    note = "Order placed successfully.",
                ),
            ),
        )

        latestOrder = order
        _orders.update { listOf(order) + it }
        cartRepository.clearCart()
        return order
    }

    override fun getRecentOrders(): List<Order> = orders.value

    override fun getOrdersForCustomer(customerId: String): List<Order> = orders.value
        .filter { order -> order.customerId == customerId }
        .sortedByDescending(Order::id)

    override fun observeOrdersForCustomer(customerId: String): Flow<List<Order>> = orders.map { allOrders ->
        allOrders.filter { it.customerId == customerId }.sortedByDescending(Order::id)
    }

    override fun getOrdersForSeller(sellerId: String): List<Order> = orders.value
        .filter { order -> order.containsSeller(normalizedSellerId(sellerId)) }
        .map { order -> order.forSeller(normalizedSellerId(sellerId)) }
        .sortedByDescending(Order::id)

    override fun observeOrdersForSeller(sellerId: String): Flow<List<Order>> = orders.map { allOrders ->
        allOrders
            .filter { it.containsSeller(normalizedSellerId(sellerId)) }
            .map { it.forSeller(normalizedSellerId(sellerId)) }
            .sortedByDescending(Order::id)
    }

    override fun getOrder(orderId: String): Order? = orders.value.firstOrNull { it.id == orderId }

    override fun getOrderForSeller(sellerId: String, orderId: String): Order? = getOrder(orderId)
        ?.takeIf { it.containsSeller(normalizedSellerId(sellerId)) }
        ?.forSeller(normalizedSellerId(sellerId))

    override suspend fun updateOrderStatus(
        orderId: String,
        sellerId: String,
        newStatus: OrderStatus,
    ): Result<Order> {
        val normalizedSellerId = normalizedSellerId(sellerId)
        val existing = getOrder(orderId)
            ?: return Result.failure(IllegalArgumentException("Order not found."))
        if (!existing.containsSeller(normalizedSellerId)) {
            return Result.failure(IllegalAccessException("You can manage only orders for your own products."))
        }
        if (existing.status == newStatus) {
            return Result.success(existing.forSeller(normalizedSellerId))
        }

        val updated = existing.copy(
            status = newStatus,
            estimatedDelivery = updatedEstimatedDelivery(existing.estimatedDelivery, newStatus),
            statusHistory = existing.statusHistory + OrderStatusEntry(
                status = newStatus,
                timestamp = formatTimestamp(LocalDateTime.now()),
                note = statusNote(newStatus),
            ),
        )

        _orders.update { currentOrders ->
            currentOrders.map { order -> if (order.id == orderId) updated else order }
        }
        if (latestOrder?.id == updated.id) {
            latestOrder = updated
        }

        if (existing.status != OrderStatus.DELIVERED && newStatus == OrderStatus.DELIVERED) {
            AppContainer.notificationService.sendOrderDeliveredNotification(
                userId = updated.customerId,
                order = updated,
            )
        }

        return Result.success(updated.forSeller(normalizedSellerId))
    }

    override fun getLatestOrder(): Order? = latestOrder

    private fun Order.containsSeller(sellerId: String): Boolean =
        items.any { item -> normalizedSellerId(item.product.storeId.ifBlank { inferredSellerId(item) }) == sellerId }

    private fun Order.forSeller(sellerId: String): Order {
        val sellerItems = items.filter { item ->
            normalizedSellerId(item.product.storeId.ifBlank { inferredSellerId(item) }) == sellerId
        }
        val sellerSubtotal = sellerItems.sumOf { it.product.price * it.quantity }
        val ratio = if (subtotal > 0.0) sellerSubtotal / subtotal else 1.0
        return copy(
            items = sellerItems,
            subtotal = sellerSubtotal,
            tax = tax * ratio,
            shippingCost = shippingCost * ratio,
            total = sellerSubtotal + (tax * ratio) + (shippingCost * ratio),
            imageUrl = sellerItems.firstOrNull()?.product?.imageUrl.orEmpty(),
        )
    }

    private fun withSeededHistory(order: Order): Order = order.copy(
        statusHistory = order.statusHistory.ifEmpty {
            listOf(
                OrderStatusEntry(
                    status = order.status,
                    timestamp = if (order.placedDate.isBlank()) formatTimestamp(LocalDateTime.now()) else "${order.placedDate} • 09:00",
                    note = statusNote(order.status),
                ),
            )
        },
    )

    private fun formatTimestamp(dateTime: LocalDateTime): String = dateTime.format(timestampFormatter)

    private fun normalizedSellerId(sellerId: String): String = AppContainer.uiPreferencesRepository.normalizeSellerStoreId(
        when (sellerId) {
            "bachir@flora.com" -> "s1"
            else -> sellerId
        },
    )

    private fun inferredSellerId(item: OrderItem): String {
        val studio = item.product.studio.uppercase()
        return when {
            studio.contains("FLORA") || studio.contains("BACHIR") -> "s1"
            studio.contains("AURUM") -> "s2"
            studio.contains("ALBA") -> "s3"
            studio.contains("ELF-READ") || studio.contains("LOOM") -> "s4"
            studio.contains("OAK & BRASS") -> "s5"
            else -> ""
        }
    }

    private fun statusNote(status: OrderStatus): String = when (status) {
        OrderStatus.PENDING -> "Awaiting seller confirmation."
        OrderStatus.CONFIRMED -> "Seller confirmed the order."
        OrderStatus.HAND_CRAFTED -> "Order is being prepared."
        OrderStatus.SHIPPED -> "Order is on the way."
        OrderStatus.DELIVERED -> "Order was marked as delivered."
        OrderStatus.CANCELLED -> "Order was cancelled."
    }

    private fun updatedEstimatedDelivery(currentValue: String, status: OrderStatus): String = when (status) {
        OrderStatus.SHIPPED -> currentValue.ifBlank { "Out for delivery soon" }
        OrderStatus.DELIVERED -> "Delivered"
        else -> currentValue
    }
}
