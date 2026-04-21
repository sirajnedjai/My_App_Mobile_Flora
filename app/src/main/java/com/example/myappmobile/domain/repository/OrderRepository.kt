package com.example.myappmobile.domain.repository

import com.example.myappmobile.domain.model.Address
import com.example.myappmobile.domain.model.CheckoutDraft
import com.example.myappmobile.domain.model.Order
import com.example.myappmobile.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface OrderRepository {
    val checkoutDraft: StateFlow<CheckoutDraft>
    val orders: StateFlow<List<Order>>

    suspend fun updateAddress(address: Address)

    suspend fun updateShippingMethod(method: String)

    suspend fun updatePaymentMethod(method: String)

    suspend fun createOrder(): Order

    suspend fun refreshCustomerOrders(): Result<Unit>

    suspend fun fetchOrderDetails(orderId: String): Result<Order>

    suspend fun refreshSellerOrders(): Result<Unit>

    suspend fun fetchSellerOrderDetails(orderId: String): Result<Order>

    fun getRecentOrders(): List<Order>

    fun getOrdersForCustomer(customerId: String): List<Order>

    fun observeOrdersForCustomer(customerId: String): Flow<List<Order>>

    fun getOrdersForSeller(sellerId: String): List<Order>

    fun observeOrdersForSeller(sellerId: String): Flow<List<Order>>

    fun getOrder(orderId: String): Order?

    fun getOrderForSeller(sellerId: String, orderId: String): Order?

    suspend fun updateOrderStatus(
        orderId: String,
        sellerId: String,
        newStatus: OrderStatus,
    ): Result<Order>

    fun getLatestOrder(): Order?
}
