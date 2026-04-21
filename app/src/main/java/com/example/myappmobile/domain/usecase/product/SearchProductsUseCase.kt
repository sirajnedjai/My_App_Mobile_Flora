package com.example.myappmobile.domain.usecase.product

import com.example.myappmobile.domain.repository.ProductRepository

class SearchProductsUseCase(private val productRepository: ProductRepository) {
    suspend operator fun invoke(query: String, category: String = "") =
        productRepository.searchProducts(query, category)
}
