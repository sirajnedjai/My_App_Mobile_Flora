package com.example.myappmobile.data.local.room.repository

import com.example.myappmobile.data.local.room.dao.ProductDao
import com.example.myappmobile.data.local.room.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao,
) {
    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAll()

    fun getProductsForSeller(sellerId: String): Flow<List<ProductEntity>> = productDao.getBySellerId(sellerId)

    suspend fun insertProduct(product: ProductEntity) {
        productDao.insert(product)
    }

    suspend fun deleteProductById(productId: String) {
        productDao.deleteById(productId)
    }

    suspend fun deleteAllProducts() {
        productDao.deleteAll()
    }
}
