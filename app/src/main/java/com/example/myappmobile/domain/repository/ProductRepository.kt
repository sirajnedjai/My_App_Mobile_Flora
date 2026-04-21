package com.example.myappmobile.domain.repository

import com.example.myappmobile.domain.Product
import com.example.myappmobile.domain.ProductDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ProductRepository {
    fun observeAllProducts(): Flow<List<Product>>

    fun observeFavoriteProducts(): Flow<List<Product>>

    val favoriteMessage: StateFlow<String?>

    val favoriteOperationProductIds: StateFlow<Set<String>>

    val isRefreshingFavorites: StateFlow<Boolean>

    suspend fun getFeaturedProducts(): List<Product>

    suspend fun getNewArrivals(): List<Product>

    suspend fun getAllProducts(): List<Product>

    suspend fun searchProducts(query: String, category: String = ""): List<Product>

    suspend fun getProductDetails(productId: String): ProductDetails

    suspend fun toggleFavorite(productId: String)

    suspend fun getFavoriteProducts(): List<Product>

    suspend fun refreshFavorites()

    fun clearFavoriteMessage()
}
