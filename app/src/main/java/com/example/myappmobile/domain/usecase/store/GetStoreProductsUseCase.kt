package com.example.myappmobile.domain.usecase.store

import com.example.myappmobile.domain.repository.StoreRepository

class GetStoreProductsUseCase(private val storeRepository: StoreRepository) {
    suspend operator fun invoke(storeId: String) = storeRepository.getStoreProducts(storeId)
}
