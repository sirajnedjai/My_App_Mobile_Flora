package com.example.myappmobile.domain.usecase.product

import com.example.myappmobile.domain.repository.ProductRepository

class GetProductDetailsUseCase(private val productRepository: ProductRepository) {
    suspend operator fun invoke(productId: String) = productRepository.getProductDetails(productId)
}
