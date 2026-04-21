package com.example.myappmobile.data.repository

import android.util.Log
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.local.dummy.DummyProducts
import com.example.myappmobile.data.local.dummy.DummyStores
import com.example.myappmobile.data.remote.StoreApiService
import com.example.myappmobile.data.remote.asObjectOrNull
import com.example.myappmobile.data.remote.extractDataElement
import com.example.myappmobile.data.remote.requireBody
import com.example.myappmobile.data.remote.string
import com.example.myappmobile.data.remote.toApiException
import com.example.myappmobile.domain.model.Review
import com.example.myappmobile.domain.model.SellerApprovalStatus
import com.example.myappmobile.domain.model.Store
import com.example.myappmobile.domain.repository.StoreRepository
import com.google.gson.Gson
import com.google.gson.JsonElement
class StoreRepositoryImpl(
    private val storeApiService: StoreApiService,
    private val gson: Gson,
) : StoreRepository {
    private val allStoreProducts = (
        DummyProducts.allProducts +
            DummyProducts.sellerProducts +
            DummyProducts.curatedWorks +
            DummyProducts.wishlistProducts
        ).distinctBy { it.id }

    override suspend fun getStoreDetails(storeId: String): Store {
        val normalizedStoreId = AppContainer.uiPreferencesRepository.normalizeSellerStoreId(storeId)
        return fetchRemoteStore(storeId, normalizedStoreId)
            ?: buildLocalFallback(storeId = storeId, normalizedStoreId = normalizedStoreId)
    }

    override suspend fun getStoreProducts(storeId: String): List<com.example.myappmobile.domain.model.Product> {
        val normalizedStoreId = AppContainer.uiPreferencesRepository.normalizeSellerStoreId(storeId)
        return allStoreProducts.filter { product ->
            AppContainer.uiPreferencesRepository.normalizeSellerStoreId(product.storeId) == normalizedStoreId
        }
    }

    override suspend fun getStoreReviews(storeId: String): List<Review> = DummyStores.storeReviews

    private suspend fun fetchRemoteStore(
        storeId: String,
        normalizedStoreId: String,
    ): Store? {
        val payload = fetchStorePayload(storeId) ?: return null
        val root = payload.asObjectOrNull() ?: return null
        val identity = resolveSellerIdentity(root)
        val storeNode = root

        val localStoreConfiguration = AppContainer.uiPreferencesRepository.getStoreConfiguration(normalizedStoreId)
        val localAccountProfile = AppContainer.uiPreferencesRepository.getAccountProfile(normalizedStoreId)

        val storeName = identity.storeName.ifBlank { localStoreConfiguration.shopName }
        val personalName = identity.personalName.ifBlank {
            localStoreConfiguration.ownerName.ifBlank { localAccountProfile.fullName }
        }
        val profileImageUrl = identity.profileImageUrl.ifBlank {
            normalizeImageUrl(
                localAccountProfile.avatarUri.ifBlank { localStoreConfiguration.logoUri },
            )
        }
        val bannerImageUrl = identity.bannerImageUrl.ifBlank {
            normalizeImageUrl(localStoreConfiguration.logoUri)
        }

        val resolvedApprovalStatus = if (identity.approvalStatus != SellerApprovalStatus.UNKNOWN) {
            identity.approvalStatus
        } else {
            AppContainer.uiPreferencesRepository.findSellerApprovalStatus(normalizedStoreId)
                ?: SellerApprovalStatus.UNKNOWN
        }

        val store = Store(
            id = storeNode.string("id", "_id", "store_id", "seller_id").orEmpty().ifBlank { normalizedStoreId },
            name = storeName,
            ownerName = personalName,
            description = storeNode.string("description", "bio", "about").orEmpty(),
            logoUrl = profileImageUrl,
            bannerUrl = bannerImageUrl,
            location = storeNode.string("address", "store_address").orEmpty(),
            contactEmail = root.string("email").orEmpty().ifBlank { localAccountProfile.email },
            rating = 0f,
            reviewCount = 0,
            practisingSince = storeNode.string("created_at", "established_at", "practising_since").orEmpty(),
            categories = emptyList(),
            story = storeNode.string("bio", "about", "description").orEmpty(),
            approvalStatus = resolvedApprovalStatus,
        )

        if (resolvedApprovalStatus != SellerApprovalStatus.UNKNOWN) {
            AppContainer.uiPreferencesRepository.saveSellerApprovalStatus(normalizedStoreId, resolvedApprovalStatus)
        }

        return store.copy(
            id = normalizedStoreId,
            name = store.name.ifBlank { normalizedStoreId },
            ownerName = store.ownerName,
            logoUrl = store.logoUrl,
            bannerUrl = store.bannerUrl.ifBlank { store.logoUrl },
        )
    }

    private suspend fun fetchStorePayload(storeId: String): JsonElement? {
        val attempts = listOf<suspend () -> JsonElement?>(
            {
                storeApiService.getStoreBySeller(storeId).requireBody(gson).data?.let(::extractDataElement)
            },
            {
                storeApiService.getStore(storeId).requireBody(gson).data?.let(::extractDataElement)
            },
        )

        attempts.forEachIndexed { index, call ->
            runCatching { call() }
                .onSuccess { payload ->
                    if (payload != null && !payload.isJsonNull) return payload
                }
                .onFailure { error ->
                    val apiError = error.toApiException()
                    Log.d(TAG, "Store fetch attempt ${index + 1} failed for $storeId: ${apiError.message}")
                    if (apiError.statusCode != 404) return null
                }
        }
        return null
    }

    private suspend fun buildLocalFallback(
        storeId: String,
        normalizedStoreId: String,
    ): Store {
        val savedConfiguration = AppContainer.uiPreferencesRepository.getStoreConfiguration(normalizedStoreId)
        val accountProfile = AppContainer.uiPreferencesRepository.getAccountProfile(normalizedStoreId)
        val baseStore = if (storeId == "s1" || normalizedStoreId == "1") DummyStores.floraCeramics else null

        val storeProducts = getStoreProducts(normalizedStoreId)
        val leadProduct = storeProducts.firstOrNull()

        val derivedStore = if (leadProduct != null) {
            Store(
                id = normalizedStoreId,
                name = leadProduct.studio,
                ownerName = "",
                description = "",
                bannerUrl = leadProduct.imageUrl,
                logoUrl = leadProduct.imageUrl,
                location = "",
                contactEmail = accountProfile.email,
                rating = 0f,
                reviewCount = 0,
                practisingSince = "",
                activeProducts = storeProducts.size,
                categories = storeProducts.map { it.category }.distinct(),
                story = "",
                approvalStatus = AppContainer.uiPreferencesRepository.findSellerApprovalStatus(normalizedStoreId)
                    ?: SellerApprovalStatus.UNKNOWN,
            )
        } else {
            (baseStore ?: DummyStores.floraCeramics.copy(id = normalizedStoreId)).copy(
                approvalStatus = AppContainer.uiPreferencesRepository.findSellerApprovalStatus(normalizedStoreId)
                    ?: SellerApprovalStatus.UNKNOWN,
            )
        }

        return derivedStore.copy(
            id = normalizedStoreId,
            name = savedConfiguration.shopName.ifBlank { derivedStore.name },
            ownerName = savedConfiguration.ownerName.ifBlank { accountProfile.fullName.ifBlank { derivedStore.ownerName } },
            description = savedConfiguration.description.ifBlank { derivedStore.description },
            logoUrl = normalizeImageUrl(
                accountProfile.avatarUri.ifBlank { savedConfiguration.logoUri.ifBlank { derivedStore.logoUrl } },
            ),
            bannerUrl = normalizeImageUrl(savedConfiguration.logoUri.ifBlank { derivedStore.bannerUrl }),
            contactEmail = accountProfile.email.ifBlank { derivedStore.contactEmail },
            practisingSince = savedConfiguration.establishmentDate.ifBlank { derivedStore.practisingSince },
            approvalStatus = AppContainer.uiPreferencesRepository.findSellerApprovalStatus(normalizedStoreId)
                ?: SellerApprovalStatus.UNKNOWN,
        )
    }

    private companion object {
        const val TAG = "StoreRepository"
    }
}
