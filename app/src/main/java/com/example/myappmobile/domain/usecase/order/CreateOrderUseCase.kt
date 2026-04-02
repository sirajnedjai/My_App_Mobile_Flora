package com.example.myappmobile.domain.usecase.order

import com.example.myappmobile.domain.repository.OrderRepository

class CreateOrderUseCase(private val orderRepository: OrderRepository) {
    suspend operator fun invoke() = orderRepository.createOrder()
}
