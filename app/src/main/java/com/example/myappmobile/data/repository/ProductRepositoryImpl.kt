package com.example.myappmobile.data.repository

import com.example.myappmobile.data.MockData
import com.example.myappmobile.domain.Product
import com.example.myappmobile.domain.ProductDetails
import com.example.myappmobile.domain.repository.ProductRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ProductRepositoryImpl : ProductRepository {

    private val mutex = Mutex()
    private var allProducts: List<Product> = MockData.allProducts

    override suspend fun getFeaturedProducts(): List<Product> = allProducts.filter {
        MockData.featuredProducts.any { featured -> featured.id == it.id }
    }

    override suspend fun getNewArrivals(): List<Product> = allProducts.filter {
        MockData.newArrivals.any { arrival -> arrival.id == it.id }
    }

    override suspend fun getAllProducts(): List<Product> = allProducts

    override suspend fun searchProducts(query: String): List<Product> = if (query.isBlank()) {
        allProducts
    } else {
        allProducts.filter { product ->
            product.name.contains(query, ignoreCase = true) ||
                product.studio.contains(query, ignoreCase = true) ||
                product.category.name.contains(query, ignoreCase = true)
        }
    }

    override suspend fun getProductDetails(productId: String): ProductDetails =
        MockData.productDetailsFor(productId).copy(
            isFavorited = allProducts.firstOrNull { it.id == productId }?.isFavorited ?: false,
        )

    override suspend fun toggleFavorite(productId: String) {
        mutex.withLock {
            allProducts = allProducts.map { product ->
                if (product.id == productId) {
                    product.copy(isFavorited = !product.isFavorited)
                } else {
                    product
                }
            }
        }
    }

    override suspend fun getFavoriteProducts(): List<Product> = allProducts.filter(Product::isFavorited)
}
