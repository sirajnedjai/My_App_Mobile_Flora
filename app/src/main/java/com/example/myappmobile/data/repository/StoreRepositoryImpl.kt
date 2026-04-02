package com.example.myappmobile.data.repository

import com.example.myappmobile.data.local.dummy.DummyProducts
import com.example.myappmobile.data.local.dummy.DummyStores
import com.example.myappmobile.domain.model.Review
import com.example.myappmobile.domain.model.Store
import com.example.myappmobile.domain.repository.StoreRepository

class StoreRepositoryImpl : StoreRepository {

    override suspend fun getStoreDetails(storeId: String): Store = DummyStores.floraCeramics

    override suspend fun getStoreProducts(storeId: String): List<com.example.myappmobile.domain.model.Product> =
        DummyProducts.allProducts.filter { product ->
            product.storeId == storeId || (storeId == "s1" && product.studio.contains("FLORA", ignoreCase = true))
        }

    override suspend fun getStoreReviews(storeId: String): List<Review> = DummyStores.storeReviews
}
