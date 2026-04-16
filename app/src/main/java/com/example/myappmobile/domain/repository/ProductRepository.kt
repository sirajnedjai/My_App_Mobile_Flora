package com.example.myappmobile.domain.repository

import com.example.myappmobile.domain.Product
import com.example.myappmobile.domain.ProductDetails
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeAllProducts(): Flow<List<Product>>

    suspend fun getFeaturedProducts(): List<Product>

    suspend fun getNewArrivals(): List<Product>

    suspend fun getAllProducts(): List<Product>

    suspend fun searchProducts(query: String): List<Product>

    suspend fun getProductDetails(productId: String): ProductDetails

    suspend fun toggleFavorite(productId: String)

    suspend fun getFavoriteProducts(): List<Product>
}
