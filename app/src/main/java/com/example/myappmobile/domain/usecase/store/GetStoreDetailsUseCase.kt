package com.example.myappmobile.domain.usecase.store

import com.example.myappmobile.domain.repository.StoreRepository

class GetStoreDetailsUseCase(private val storeRepository: StoreRepository) {
    suspend operator fun invoke(storeId: String) = storeRepository.getStoreDetails(storeId)
}
