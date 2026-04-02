package com.example.myappmobile.data.repository

import com.example.myappmobile.data.local.dummy.DummyOrders
import com.example.myappmobile.domain.model.Address
import com.example.myappmobile.domain.model.CheckoutDraft
import com.example.myappmobile.domain.model.Order
import com.example.myappmobile.domain.model.OrderItem
import com.example.myappmobile.domain.model.OrderStatus
import com.example.myappmobile.domain.repository.CartRepository
import com.example.myappmobile.domain.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OrderRepositoryImpl(
    private val cartRepository: CartRepository,
) : OrderRepository {

    private val _checkoutDraft = MutableStateFlow(
        CheckoutDraft(address = DummyOrders.address1)
    )
    override val checkoutDraft: StateFlow<CheckoutDraft> = _checkoutDraft.asStateFlow()

    private val recentOrders = mutableListOf<Order>().apply {
        addAll(DummyOrders.recentOrders)
    }

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
        val items = cartRepository.getCheckoutItems()
        val subtotal = items.sumOf { it.product.price * it.quantity }
        val shippingCost = if (_checkoutDraft.value.shippingMethod.contains("Concierge")) 25.0 else 12.0
        val tax = subtotal * 0.08

        val order = Order(
            id = "order_${System.currentTimeMillis()}",
            reference = "ATL-${(100000..999999).random()}",
            customerName = _checkoutDraft.value.address?.fullName.orEmpty(),
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
            placedDate = "Today",
            estimatedDelivery = "3-5 business days",
        )

        latestOrder = order
        recentOrders.add(0, order)
        cartRepository.clearCart()
        return order
    }

    override fun getRecentOrders(): List<Order> = recentOrders.toList()

    override fun getLatestOrder(): Order? = latestOrder
}
