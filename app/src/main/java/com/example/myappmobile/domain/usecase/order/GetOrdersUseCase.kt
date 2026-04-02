package com.example.myappmobile.domain.usecase.order

import com.example.myappmobile.domain.repository.OrderRepository

class GetOrdersUseCase(private val orderRepository: OrderRepository) {
    operator fun invoke() = orderRepository.getRecentOrders()
}
