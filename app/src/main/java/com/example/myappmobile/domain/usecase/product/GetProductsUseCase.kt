package com.example.myappmobile.domain.usecase.product

import com.example.myappmobile.domain.repository.ProductRepository

class GetProductsUseCase(private val productRepository: ProductRepository) {
    suspend fun featured() = productRepository.getFeaturedProducts()

    suspend fun newArrivals() = productRepository.getNewArrivals()

    suspend fun all() = productRepository.getAllProducts()
}
