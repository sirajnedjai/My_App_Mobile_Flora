package com.example.myappmobile.domain.repository

import com.example.myappmobile.domain.model.Review
import com.example.myappmobile.domain.model.Store

interface StoreRepository {
    suspend fun getStoreDetails(storeId: String): Store

    suspend fun getStoreProducts(storeId: String): List<com.example.myappmobile.domain.model.Product>

    suspend fun getStoreReviews(storeId: String): List<Review>
}
