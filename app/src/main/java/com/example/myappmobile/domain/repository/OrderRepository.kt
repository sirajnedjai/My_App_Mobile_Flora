package com.example.myappmobile.domain.repository

import com.example.myappmobile.domain.model.Address
import com.example.myappmobile.domain.model.CheckoutDraft
import com.example.myappmobile.domain.model.Order
import kotlinx.coroutines.flow.StateFlow

interface OrderRepository {
    val checkoutDraft: StateFlow<CheckoutDraft>

    suspend fun updateAddress(address: Address)

    suspend fun updateShippingMethod(method: String)

    suspend fun updatePaymentMethod(method: String)

    suspend fun createOrder(): Order

    fun getRecentOrders(): List<Order>

    fun getLatestOrder(): Order?
}
