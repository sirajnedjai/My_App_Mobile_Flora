package com.example.myappmobile.data.repository

import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.local.dummy.DummyProducts
import com.example.myappmobile.data.local.dummy.DummyStores
import com.example.myappmobile.domain.model.Review
import com.example.myappmobile.domain.model.Store
import com.example.myappmobile.domain.repository.StoreRepository

class StoreRepositoryImpl : StoreRepository {
    private val allStoreProducts = (
        DummyProducts.allProducts +
            DummyProducts.sellerProducts +
            DummyProducts.curatedWorks +
            DummyProducts.wishlistProducts
        ).distinctBy { it.id }

    override suspend fun getStoreDetails(storeId: String): Store {
        val normalizedStoreId = AppContainer.uiPreferencesRepository.normalizeSellerStoreId(storeId)
        val savedConfiguration = AppContainer.uiPreferencesRepository.getStoreConfiguration(normalizedStoreId)
        val accountProfile = AppContainer.uiPreferencesRepository.getAccountProfile(storeId)
        val baseStore = if (storeId == "s1" || normalizedStoreId == "1") DummyStores.floraCeramics else null

        val storeProducts = allStoreProducts.filter { product ->
            AppContainer.uiPreferencesRepository.normalizeSellerStoreId(product.storeId) == normalizedStoreId
        }
        val leadProduct = storeProducts.firstOrNull()

        val derivedStore = if (leadProduct != null) {
            Store(
                id = normalizedStoreId,
                name = leadProduct.studio,
                ownerName = leadProduct.studio,
                description = "A curated FLORA studio presenting handcrafted ${leadProduct.category.lowercase()} pieces for modern collectors.",
                bannerUrl = leadProduct.imageUrl,
                logoUrl = leadProduct.imageUrl,
                location = "FLORA Atelier Network",
                contactEmail = "${leadProduct.studio.lowercase().replace(" ", "").replace("&", "and")}@flora.com",
                rating = 4.8f,
                reviewCount = (18..96).random(),
                practisingSince = "2016",
                activeProducts = storeProducts.size,
                categories = storeProducts.map { it.category }.distinct(),
                story = "${leadProduct.studio} is part of the FLORA marketplace, offering small-batch artisan work with an emphasis on material quality and quiet luxury.",
                approvalStatus = AppContainer.uiPreferencesRepository.getSellerApprovalStatus(normalizedStoreId),
            )
        } else {
            (baseStore ?: DummyStores.floraCeramics.copy(id = normalizedStoreId)).copy(
                approvalStatus = AppContainer.uiPreferencesRepository.getSellerApprovalStatus(normalizedStoreId),
            )
        }

        return derivedStore.copy(
            id = normalizedStoreId,
            name = savedConfiguration.shopName.ifBlank { derivedStore.name },
            ownerName = savedConfiguration.ownerName.ifBlank {
                accountProfile.fullName.ifBlank { derivedStore.ownerName }
            },
            description = savedConfiguration.description.ifBlank { derivedStore.description },
            logoUrl = accountProfile.avatarUri.ifBlank {
                savedConfiguration.logoUri.ifBlank { derivedStore.logoUrl }
            },
            bannerUrl = savedConfiguration.logoUri.ifBlank { derivedStore.bannerUrl },
            contactEmail = accountProfile.email.ifBlank { derivedStore.contactEmail },
            practisingSince = savedConfiguration.establishmentDate.ifBlank { derivedStore.practisingSince },
            approvalStatus = AppContainer.uiPreferencesRepository.getSellerApprovalStatus(normalizedStoreId),
        )
    }

    override suspend fun getStoreProducts(storeId: String): List<com.example.myappmobile.domain.model.Product> =
        allStoreProducts.filter { product ->
            val normalizedStoreId = AppContainer.uiPreferencesRepository.normalizeSellerStoreId(storeId)
            AppContainer.uiPreferencesRepository.normalizeSellerStoreId(product.storeId) == normalizedStoreId
        }

    override suspend fun getStoreReviews(storeId: String): List<Review> = DummyStores.storeReviews
}
