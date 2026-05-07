package com.example.myappmobile.data.repository

import android.util.Log
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.local.room.DatabaseProvider
import com.example.myappmobile.data.remote.ApiException
import com.example.myappmobile.data.remote.StoreApiService
import com.example.myappmobile.data.remote.asObjectOrNull
import com.example.myappmobile.data.remote.extractDataElement
import com.example.myappmobile.data.remote.requireBody
import com.example.myappmobile.data.remote.rethrowIfCancellation
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
    private val productDao by lazy { DatabaseProvider.getDatabase().productDao() }

    override suspend fun getStoreDetails(storeId: String): Store {
        val normalizedStoreId = AppContainer.uiPreferencesRepository.normalizeSellerStoreId(storeId)
        return fetchRemoteStore(storeId, normalizedStoreId)
            ?: buildLocalFallback(normalizedStoreId)
            ?: throw ApiException("Store details are unavailable right now. Please try again later.")
    }

    override suspend fun getStoreProducts(storeId: String): List<com.example.myappmobile.domain.model.Product> {
        val normalizedStoreId = AppContainer.uiPreferencesRepository.normalizeSellerStoreId(storeId)
        return productDao.getAllOnce()
            .filter { product ->
                AppContainer.uiPreferencesRepository.normalizeSellerStoreId(product.sellerId) == normalizedStoreId
            }
            .map { entity ->
                com.example.myappmobile.domain.model.Product(
                    id = entity.id,
                    name = entity.name,
                    price = entity.price,
                    imageUrl = entity.imageUrl,
                    studio = entity.studio,
                    storeId = entity.sellerId,
                    category = entity.category,
                    description = entity.description,
                    stockCount = entity.stockCount,
                    isFavorited = entity.isFavorited,
                    collectionLabel = entity.category,
                    story = entity.description,
                    images = listOf(entity.imageUrl).filter { it.isNotBlank() },
                )
            }
    }

    override suspend fun getStoreReviews(storeId: String): List<Review> = emptyList()

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
            bannerUrl = store.bannerUrl,
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
                    error.rethrowIfCancellation()
                    val apiError = error.toApiException()
                    Log.d(TAG, "Store fetch attempt ${index + 1} failed for $storeId: ${apiError.message}")
                    if (apiError.statusCode != 404) return null
                }
        }
        return null
    }

    private suspend fun buildLocalFallback(
        normalizedStoreId: String,
    ): Store? {
        val savedConfiguration = AppContainer.uiPreferencesRepository.getStoreConfiguration(normalizedStoreId)
        val accountProfile = AppContainer.uiPreferencesRepository.getAccountProfile(normalizedStoreId)
        val storeProducts = getStoreProducts(normalizedStoreId)
        val hasLocalStoreData = savedConfiguration.shopName.isNotBlank() ||
            savedConfiguration.description.isNotBlank() ||
            savedConfiguration.logoUri.isNotBlank() ||
            accountProfile.fullName.isNotBlank() ||
            accountProfile.email.isNotBlank() ||
            storeProducts.isNotEmpty()
        if (!hasLocalStoreData) {
            return null
        }

        val derivedName = savedConfiguration.shopName.ifBlank {
            storeProducts.firstOrNull()?.studio.orEmpty()
        }
        val derivedOwner = savedConfiguration.ownerName.ifBlank {
            accountProfile.fullName
        }
        val derivedLogo = normalizeImageUrl(
            accountProfile.avatarUri.ifBlank { savedConfiguration.logoUri },
        )
        val derivedBanner = normalizeImageUrl(savedConfiguration.logoUri)

        return Store(
            id = normalizedStoreId,
            name = derivedName,
            ownerName = derivedOwner,
            description = savedConfiguration.description,
            logoUrl = derivedLogo,
            bannerUrl = derivedBanner,
            location = accountProfile.address,
            contactEmail = accountProfile.email,
            rating = 0f,
            reviewCount = 0,
            practisingSince = savedConfiguration.establishmentDate,
            activeProducts = storeProducts.size,
            categories = storeProducts.map { it.category }.distinct(),
            story = savedConfiguration.description,
            approvalStatus = AppContainer.uiPreferencesRepository.findSellerApprovalStatus(normalizedStoreId)
                ?: SellerApprovalStatus.UNKNOWN,
        )
    }

    private companion object {
        const val TAG = "StoreRepository"
    }
}
