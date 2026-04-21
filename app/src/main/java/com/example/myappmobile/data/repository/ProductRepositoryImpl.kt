package com.example.myappmobile.data.repository

import android.net.Uri
import android.util.Log
import com.example.myappmobile.BuildConfig
import com.example.myappmobile.data.local.room.DatabaseProvider
import com.example.myappmobile.data.local.room.entity.ProductEntity
import com.example.myappmobile.data.mapper.ProductEntityMapper
import com.example.myappmobile.data.remote.FavoriteApiService
import com.example.myappmobile.data.remote.ProductApiService
import com.example.myappmobile.data.remote.ProductDto
import com.example.myappmobile.data.remote.ReviewDto
import com.example.myappmobile.data.remote.StoreDto
import com.example.myappmobile.data.remote.FavoriteDto
import com.example.myappmobile.data.remote.FavoriteRequestDto
import com.example.myappmobile.data.remote.arrayAt
import com.example.myappmobile.data.remote.asArrayOrNull
import com.example.myappmobile.data.remote.asObjectOrNull
import com.example.myappmobile.data.remote.asStringOrNull
import com.example.myappmobile.data.remote.double
import com.example.myappmobile.data.remote.element
import com.example.myappmobile.data.remote.extractDataElement
import com.example.myappmobile.data.remote.int
import com.example.myappmobile.data.remote.objectAt
import com.example.myappmobile.data.remote.requireBody
import com.example.myappmobile.data.remote.string
import com.example.myappmobile.data.remote.toApiException
import com.example.myappmobile.domain.ArtistProfile
import com.example.myappmobile.domain.Product
import com.example.myappmobile.domain.ProductDetails
import com.example.myappmobile.domain.Review
import com.example.myappmobile.domain.model.SellerApprovalStatus
import com.example.myappmobile.domain.repository.AuthRepository
import com.example.myappmobile.domain.repository.ProductRepository
import com.example.myappmobile.domain.repository.StoreRepository
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ProductRepositoryImpl(
    private val productApiService: ProductApiService,
    private val favoriteApiService: FavoriteApiService,
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository,
    private val gson: Gson,
) : ProductRepository {

    private val productDao by lazy { DatabaseProvider.getDatabase().productDao() }
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val syncMutex = Mutex()
    private val favoriteMutex = Mutex()
    private val serverBaseUrl = BuildConfig.FLORA_API_BASE_URL.removeSuffix("/api/")
    private val _favoriteMessage = MutableStateFlow<String?>(null)
    override val favoriteMessage: StateFlow<String?> = _favoriteMessage.asStateFlow()
    private val _favoriteOperationProductIds = MutableStateFlow<Set<String>>(emptySet())
    override val favoriteOperationProductIds: StateFlow<Set<String>> = _favoriteOperationProductIds.asStateFlow()
    private val _isRefreshingFavorites = MutableStateFlow(false)
    override val isRefreshingFavorites: StateFlow<Boolean> = _isRefreshingFavorites.asStateFlow()

    private companion object {
        const val TAG = "ProductRepository"
    }

    init {
        repositoryScope.launch {
            authRepository.currentUser.collectLatest { user ->
                if (user.isAuthenticated) {
                    refreshProducts()
                    refreshFavorites()
                } else {
                    clearFavoriteFlags()
                    _favoriteMessage.value = null
                }
            }
        }
    }

    override fun observeAllProducts(): Flow<List<Product>> {
        Log.d(TAG, "observeAllProducts invoked")
        triggerSync()
        return productDao.getAll().map { products ->
            products.map(ProductEntityMapper::toDomainProduct)
        }
    }

    override fun observeFavoriteProducts(): Flow<List<Product>> = observeAllProducts().map { products ->
        products.filter(Product::isFavorited)
    }

    override suspend fun getFeaturedProducts(): List<Product> {
        refreshProducts()
        return productDao.getAllOnce()
            .filter(ProductEntity::isFeatured)
            .map(ProductEntityMapper::toDomainProduct)
            .ifEmpty { getAllProducts().take(4) }
    }

    override suspend fun getNewArrivals(): List<Product> {
        refreshProducts()
        return productDao.getAllOnce()
            .filter(ProductEntity::isNewArrival)
            .sortedByDescending(ProductEntity::createdAt)
            .map(ProductEntityMapper::toDomainProduct)
            .ifEmpty { getAllProducts().take(4) }
    }

    override suspend fun getAllProducts(): List<Product> {
        refreshProducts()
        return observeAllProducts().first()
    }

    override suspend fun searchProducts(query: String, category: String): List<Product> {
        refreshProducts()
        val normalizedQuery = query.trim()
        val normalizedCategory = category.trim()
        Log.d(TAG, "Product search requested. keyword=$normalizedQuery, category=$normalizedCategory")

        if (normalizedQuery.isBlank() && normalizedCategory.isBlank()) {
            return getAllProducts()
        }

        return runCatching {
            val favoritesById = productDao.getAllOnce().associate { it.id to it.isFavorited }
            val dtos = when {
                normalizedQuery.isNotBlank() -> {
                    val response = productApiService.searchProducts(normalizedQuery).requireBody(gson)
                    Log.d(TAG, "Search endpoint success. keyword=$normalizedQuery")
                    extractProducts(response.data)
                }
                else -> {
                    val response = productApiService.filterProducts(normalizedCategory).requireBody(gson)
                    Log.d(TAG, "Filter endpoint success. category=$normalizedCategory")
                    extractProducts(response.data)
                }
            }

            val mapped = dtos.mapIndexed { index, dto ->
                ProductEntityMapper.toDomainProduct(
                    dto.toEntity(
                        existingFavorite = favoritesById[dto.id.asStringOrNull().orEmpty()] == true,
                        index = index,
                    ),
                )
            }
            val categoryFiltered = if (normalizedQuery.isNotBlank() && normalizedCategory.isNotBlank()) {
                mapped.filter { product ->
                    product.category.name.equals(normalizedCategory, ignoreCase = true) ||
                        product.category.name.contains(normalizedCategory, ignoreCase = true)
                }
            } else {
                mapped
            }
            Log.d(TAG, "Search/filter results mapped. count=${categoryFiltered.size}")
            categoryFiltered
        }.getOrElse { error ->
            val apiError = error.toApiException()
            Log.d(TAG, "Search/filter endpoint failed. keyword=$normalizedQuery category=$normalizedCategory error=${apiError.message}")
            val local = getAllProducts().filter { product ->
                val queryMatch = normalizedQuery.isBlank() ||
                    product.name.contains(normalizedQuery, ignoreCase = true) ||
                    product.studio.contains(normalizedQuery, ignoreCase = true) ||
                    product.category.name.contains(normalizedQuery, ignoreCase = true)
                val categoryMatch = normalizedCategory.isBlank() ||
                    product.category.name.equals(normalizedCategory, ignoreCase = true) ||
                    product.category.name.contains(normalizedCategory, ignoreCase = true)
                queryMatch && categoryMatch
            }
            Log.d(TAG, "Falling back to cached local search/filter. count=${local.size}")
            local
        }
    }

    override suspend fun getProductDetails(productId: String): ProductDetails {
        require(productId.isNotBlank()) { "Product id is missing." }
        Log.d(TAG, "Requesting product details. productId=$productId")
        refreshProducts()
        val response = runCatching {
            productApiService.getProductDetails(productId).requireBody(gson)
        }.getOrElse { error ->
            val apiError = error.toApiException()
            Log.d(TAG, "Product details endpoint failed. productId=$productId error=${apiError.message}")
            return buildCachedDetails(productId) ?: throw apiError
        }

        val payload = extractDataElement(response.data)
            ?: return buildCachedDetails(productId)
            ?: throw IllegalStateException("Product details are unavailable for this item.")
        val productDto = gson.fromJson(payload, ProductDto::class.java)
        val productJson = payload.asObjectOrNull()
        Log.d(
            TAG,
            "Product details payload image mapping. productId=$productId rawImage=${productDto.image} imageUrl=${productDto.imageUrl} jsonImageUrl=${productJson?.string("image_url")}",
        )
        val entity = productDto.toEntity(
            existingFavorite = productDao.getById(productId)?.isFavorited ?: false,
            fallbackId = productId,
        )
        productDao.insert(entity)

        val allProducts = productDao.getAllOnce()
        val baseProduct = ProductEntityMapper.toDomainProduct(entity)
        val similar = allProducts
            .filter { it.id != entity.id }
            .sortedWith(
                compareByDescending<ProductEntity> { it.category.equals(entity.category, ignoreCase = true) }
                    .thenByDescending { it.isFeatured }
                    .thenByDescending { it.createdAt },
            )
            .take(6)
            .map(ProductEntityMapper::toDomainProduct)

        val storeDto = productJson?.element("store", "seller")?.let { gson.fromJson(it, StoreDto::class.java) }
        val inlineReviews = productJson?.arrayAt("reviews")?.mapNotNull { reviewElement ->
            runCatching { gson.fromJson(reviewElement, ReviewDto::class.java).toDomainReview() }.getOrNull()
        }.orEmpty()
        val endpointReviews = fetchReviews(productId)
        val mergedReviews = (endpointReviews + inlineReviews).distinctBy(Review::id)

        val storeObject = productJson?.objectAt("store", "seller")
        val inlineSellerIdentity = resolveSellerIdentity(storeObject)
        val resolvedSellerId = entity.sellerId.ifBlank { storeDto?.id.asStringOrNull().orEmpty() }
        val storeDetails = if (
            resolvedSellerId.isNotBlank() &&
            (inlineSellerIdentity.approvalStatus == SellerApprovalStatus.UNKNOWN ||
                inlineSellerIdentity.storeName.isBlank() ||
                inlineSellerIdentity.profileImageUrl.isBlank())
        ) {
            runCatching { storeRepository.getStoreDetails(resolvedSellerId) }.getOrNull()
        } else {
            null
        }
        val sellerIdentity = inlineSellerIdentity.copy(
            storeName = inlineSellerIdentity.storeName.ifBlank { storeDetails?.name.orEmpty() },
            personalName = inlineSellerIdentity.personalName.ifBlank { storeDetails?.ownerName.orEmpty() },
            profileImageUrl = inlineSellerIdentity.profileImageUrl.ifBlank { storeDetails?.logoUrl.orEmpty() },
            bannerImageUrl = inlineSellerIdentity.bannerImageUrl.ifBlank { storeDetails?.bannerUrl.orEmpty() },
            approvalStatus = if (inlineSellerIdentity.approvalStatus != SellerApprovalStatus.UNKNOWN) {
                inlineSellerIdentity.approvalStatus
            } else {
                storeDetails?.approvalStatus ?: SellerApprovalStatus.UNKNOWN
            },
        )
        val artistName = sellerIdentity.personalName.ifBlank {
            storeDto?.ownerName.orEmpty().ifBlank {
                storeObject?.string("owner_name", "seller_name", "full_name").orEmpty()
            }
        }
        val studioName = sellerIdentity.storeName.ifBlank {
            storeDto?.name.orEmpty().ifBlank {
                storeObject?.string("name", "store_name", "shop_name").orEmpty()
            }.ifBlank { entity.studio }
        }
        val avatarUrl = sellerIdentity.profileImageUrl.ifBlank {
            normalizeImageUrl(
                storeDto?.logoUrl
                    ?: storeDto?.bannerUrl
                    ?: storeObject?.string("logo", "logo_url", "avatar", "avatar_url", "image", "banner", "banner_url")
                    ?: entity.imageUrl,
            )
        }

        return ProductDetails(
            id = baseProduct.id,
            sellerId = entity.sellerId,
            name = baseProduct.name,
            collectionLabel = baseProduct.category.name.uppercase(),
            price = baseProduct.price,
            story = productJson?.string("story", "description", "details").orEmpty().ifBlank { entity.description },
            material = productJson?.string("material").orEmpty(),
            dimensions = productJson?.string("dimensions", "size").orEmpty(),
            images = resolveImages(productDto, productJson, entity.imageUrl),
            artist = ArtistProfile(
                id = resolvedSellerId,
                name = artistName,
                avatarUrl = avatarUrl,
                rating = (productJson?.double("rating", "average_rating") ?: 0.0).toFloat(),
                reviewCount = productJson?.int("review_count", "reviews_count") ?: mergedReviews.size,
                studioName = studioName,
                sellerApprovalStatus = sellerIdentity.approvalStatus,
            ),
            reviews = mergedReviews,
            similarProducts = similar,
            isFavorited = baseProduct.isFavorited,
        ).also { details ->
            Log.d(
                TAG,
                "Product details mapped successfully. id=${details.id}, name=${details.name}, images=${details.images.size}, sellerId=${details.sellerId}",
            )
        }
    }

    override suspend fun toggleFavorite(productId: String) {
        if (!authRepository.currentUser.value.isAuthenticated) {
            _favoriteMessage.value = "Your session has expired. Please sign in again."
            return
        }
        if (productId.isBlank() || productId in _favoriteOperationProductIds.value) return

        favoriteMutex.withLock {
            val existing = productDao.getById(productId)
            val previousValue = existing?.isFavorited == true
            val shouldAdd = !previousValue

            _favoriteOperationProductIds.value = _favoriteOperationProductIds.value + productId
            setFavoriteFlag(productId, shouldAdd)

            runCatching {
                val response = if (shouldAdd) {
                    favoriteApiService.addFavorite(
                        FavoriteRequestDto(productId = productId),
                    ).requireBody(gson)
                } else {
                    favoriteApiService.deleteFavorite(productId).requireBody(gson)
                }
                val syncedFavorites = syncFavoritesLocked()
                verifyFavoriteSync(
                    productId = productId,
                    shouldBeFavorited = shouldAdd,
                    favoritePayload = syncedFavorites,
                )
                _favoriteMessage.value = response.message
            }.onFailure { throwable ->
                val apiError = throwable.toApiException()
                setFavoriteFlag(productId, previousValue)
                if (apiError.statusCode == 401) {
                    clearFavoriteFlags()
                }
                _favoriteMessage.value = apiError.message
            }

            _favoriteOperationProductIds.value = _favoriteOperationProductIds.value - productId
        }
    }

    override suspend fun getFavoriteProducts(): List<Product> {
        if (authRepository.currentUser.value.isAuthenticated) {
            refreshFavorites()
        }
        return productDao.getAllOnce()
            .filter(ProductEntity::isFavorited)
            .map(ProductEntityMapper::toDomainProduct)
    }

    override suspend fun refreshFavorites() {
        if (!authRepository.currentUser.value.isAuthenticated) {
            clearFavoriteFlags()
            return
        }

        favoriteMutex.withLock {
            runCatching { syncFavoritesLocked() }
        }
    }

    override fun clearFavoriteMessage() {
        _favoriteMessage.value = null
    }

    private suspend fun syncFavoritesLocked(): FavoritePayload {
        _isRefreshingFavorites.value = true
        return try {
            val response = favoriteApiService.getFavorites().requireBody(gson)
            val favoritePayload = extractFavoriteEntries(response.data)
            if (favoritePayload.products.isNotEmpty()) {
                val currentFavorites = productDao.getAllOnce().associate { it.id to it.isFavorited }
                val incomingProducts = favoritePayload.products.mapIndexed { index, dto ->
                    dto.toEntity(
                        existingFavorite = currentFavorites[dto.id.asStringOrNull().orEmpty()] == true,
                        index = index,
                    )
                }
                productDao.insertAll(incomingProducts)
            }
            applyFavoriteFlags(favoritePayload.productIds)
            favoritePayload
        } catch (throwable: Throwable) {
            val apiError = throwable.toApiException()
            if (apiError.statusCode == 401) {
                clearFavoriteFlags()
            }
            _favoriteMessage.value = apiError.message
            throw apiError
        } finally {
            _isRefreshingFavorites.value = false
        }
    }

    private fun triggerSync() {
        repositoryScope.launch {
            Log.d(TAG, "triggerSync launched")
            refreshProducts()
        }
    }

    private suspend fun refreshProducts() {
        syncMutex.withLock {
            runCatching {
                val response = productApiService.getProducts().requireBody(gson)
                val remoteProducts = extractProducts(response.data)
                if (remoteProducts.isNotEmpty()) {
                    val favoritesById = productDao.getAllOnce().associate { it.id to it.isFavorited }
                    productDao.deleteAll()
                    productDao.insertAll(
                        remoteProducts.mapIndexed { index, dto ->
                            dto.toEntity(existingFavorite = favoritesById[dto.id.asStringOrNull().orEmpty()] == true, index = index)
                        },
                    )
                    Log.d(TAG, "Products endpoint loaded successfully. count=${remoteProducts.size}")
                    if (authRepository.currentUser.value.isAuthenticated) {
                        refreshFavorites()
                    }
                } else {
                    Log.d(TAG, "Products endpoint returned no items.")
                }
            }.onFailure { error ->
                Log.d(TAG, "Products endpoint failed: ${error.toApiException().message}")
            }
        }
    }

    private suspend fun extractProducts(data: JsonElement?): List<ProductDto> {
        val listElement = extractDataElement(data)
        return when {
            listElement == null -> emptyList()
            listElement.isJsonArray -> listElement.asJsonArray.mapNotNull { element ->
                runCatching { gson.fromJson(element, ProductDto::class.java) }.getOrNull()
            }
            listElement.asObjectOrNull() != null -> {
                listElement.asObjectOrNull()?.arrayAt("data", "items", "products")?.mapNotNull { element ->
                    runCatching { gson.fromJson(element, ProductDto::class.java) }.getOrNull()
                }.orEmpty()
            }
            else -> emptyList()
        }
    }

    private suspend fun fetchReviews(productId: String): List<Review> = runCatching {
        val response = productApiService.getProductReviews(productId).requireBody(gson)
        val data = extractDataElement(response.data)
        when {
            data == null -> emptyList()
            data.isJsonArray -> data.asJsonArray.mapNotNull { element ->
                runCatching { gson.fromJson(element, ReviewDto::class.java).toDomainReview() }.getOrNull()
            }
            data.asObjectOrNull() != null -> data.asObjectOrNull()?.arrayAt("reviews", "data", "items")?.mapNotNull { element ->
                runCatching { gson.fromJson(element, ReviewDto::class.java).toDomainReview() }.getOrNull()
            }.orEmpty()
            else -> emptyList()
        }
    }.getOrDefault(emptyList())

    private suspend fun applyFavoriteFlags(favoriteIds: Set<String>) {
        val current = productDao.getAllOnce()
        if (current.isEmpty()) return
        val updated = current.map { entity ->
            entity.copy(isFavorited = entity.id in favoriteIds)
        }
        productDao.insertAll(updated)
    }

    private suspend fun setFavoriteFlag(productId: String, isFavorited: Boolean) {
        val current = productDao.getById(productId) ?: return
        if (current.isFavorited == isFavorited) return
        productDao.insert(current.copy(isFavorited = isFavorited))
    }

    private suspend fun clearFavoriteFlags() {
        val current = productDao.getAllOnce()
        if (current.none(ProductEntity::isFavorited)) return
        productDao.insertAll(current.map { it.copy(isFavorited = false) })
    }

    private fun extractFavoriteEntries(data: JsonElement?): FavoritePayload {
        val payload = extractDataElement(data) ?: data
        if (payload == null || payload.isJsonNull) {
            return FavoritePayload(emptySet(), emptyList())
        }

        val elements = when {
            payload.isJsonArray -> payload.asJsonArray.toList()
            payload.asObjectOrNull() != null -> payload.asObjectOrNull()
                ?.arrayAt("favorites", "data", "items", "products")
                ?.toList()
                ?: listOfNotNull(
                    payload.asObjectOrNull()?.element("favorite", "item", "product"),
                )
            else -> emptyList()
        }

        val favoriteIds = elements.mapNotNull(::extractFavoriteProductId).toSet()
        val nestedProducts = elements.mapNotNull(::extractFavoriteProduct)
        return FavoritePayload(
            productIds = favoriteIds,
            products = nestedProducts,
        )
    }

    private fun extractFavoriteProductId(element: JsonElement): String? {
        val directFavorite = runCatching { gson.fromJson(element, FavoriteDto::class.java) }.getOrNull()
        val objectElement = element.asObjectOrNull()
        val nestedProductObject = objectElement?.objectAt("product", "item")

        return directFavorite?.productId?.asStringOrNull()
            ?: objectElement?.string("product_id", "productId", "id_product")
            ?: nestedProductObject?.string("id", "_id", "product_id")
            ?: objectElement?.takeIf(::looksLikeProductObject)?.string("id", "_id", "product_id")
            ?: directFavorite?.id?.asStringOrNull()
    }

    private fun extractFavoriteProduct(element: JsonElement): ProductDto? {
        val objectElement = element.asObjectOrNull() ?: return null
        val productElement = objectElement.element("product", "item")
            ?: objectElement.takeIf(::looksLikeProductObject)
            ?: return null
        return runCatching { gson.fromJson(productElement, ProductDto::class.java) }.getOrNull()
    }

    private fun looksLikeProductObject(obj: JsonObject): Boolean =
        obj.string("name", "title") != null ||
            obj.element("price", "amount", "sale_price") != null ||
            obj.string("image", "image_url", "thumbnail", "photo") != null

    private fun verifyFavoriteSync(
        productId: String,
        shouldBeFavorited: Boolean,
        favoritePayload: FavoritePayload,
    ) {
        val isFavoritedOnServer = productId in favoritePayload.productIds
        if (shouldBeFavorited == isFavoritedOnServer) return

        throw IllegalStateException(
            if (shouldBeFavorited) {
                "The product was not returned in your wishlist after the server refresh."
            } else {
                "The product still appears in your wishlist after the server refresh."
            },
        )
    }

    private suspend fun buildCachedDetails(productId: String): ProductDetails? {
        val catalog = productDao.getAllOnce()
        val target = catalog.firstOrNull { it.id == productId } ?: return null
        val reviews = fetchReviews(productId)
        return ProductDetails(
            id = target.id,
            sellerId = target.sellerId,
            name = target.name,
            collectionLabel = target.category.uppercase(),
            price = target.price,
            story = target.description,
            material = "",
            dimensions = "",
            images = listOf(target.imageUrl).filter { it.isNotBlank() },
            artist = ArtistProfile(
                id = target.sellerId,
                name = target.studio.ifBlank { "FLORA Seller" },
                avatarUrl = target.imageUrl,
                rating = 0f,
                reviewCount = reviews.size,
                studioName = target.studio.ifBlank { "FLORA Seller" },
            ),
            reviews = reviews,
            similarProducts = catalog
                .filter { it.id != target.id }
                .take(6)
                .map(ProductEntityMapper::toDomainProduct),
            isFavorited = target.isFavorited,
        ).also {
            Log.d(TAG, "Using cached product details fallback. productId=$productId")
        }
    }

    private fun resolveImages(
        productDto: ProductDto,
        productJson: JsonObject?,
        primaryImage: String,
    ): List<String> = buildList {
        add(primaryImage)
        add(resolveProductImageUrl(productDto))
        add(normalizeImageUrl(productDto.image.orEmpty()))
        productDto.images.asArrayOrNull()?.forEach { image ->
            add(normalizeImageUrl(image.asStringOrNull().orEmpty()))
        }
        productJson?.arrayAt("gallery", "photos")?.forEach { image ->
            add(normalizeImageUrl(image.asStringOrNull().orEmpty()))
        }
    }.filter { it.isNotBlank() }.distinct()

    private fun ProductDto.toEntity(
        existingFavorite: Boolean = false,
        index: Int = 0,
        fallbackId: String = "",
    ): ProductEntity {
        val rawId = id.asStringOrNull().orEmpty()
            .ifBlank { fallbackId }
            .ifBlank { "remote_$index" }
        val storeObject = store.asObjectOrNull() ?: seller.asObjectOrNull()
        val categoryObject = category.asObjectOrNull()
        val primaryImage = resolveProductImageUrl(this).ifBlank {
            normalizeImageUrl(
                images.asArrayOrNull()?.firstOrNull()?.asStringOrNull()
                    ?: "",
            )
        }
        Log.d(
            TAG,
            "Product mapped. id=$rawId title=${name.orEmpty()} rawImage=$image imageUrl=$imageUrl finalImage=$primaryImage",
        )

        return ProductEntity(
            id = rawId,
            sellerId = storeId.asStringOrNull().orEmpty().ifBlank {
                storeObject?.string("id", "_id", "seller_id", "store_id").orEmpty()
            },
            name = name.orEmpty().ifBlank { "FLORA Piece" },
            description = description.orEmpty(),
            price = price.asStringOrNull()?.toDoubleOrNull() ?: 0.0,
            imageUrl = primaryImage,
            category = categoryObject?.string("name", "title")
                ?: category.asStringOrNull().orEmpty(),
            studio = storeObject?.string("name", "store_name", "shop_name", "seller_name")
                ?: "FLORA Atelier",
            stockCount = stock.asStringOrNull()?.toIntOrNull() ?: 0,
            isFavorited = isFavorite == true || existingFavorite,
            isFeatured = this.isFeatured == true || index < 4,
            isNewArrival = this.isNewArrival == true || index < 4,
            createdAt = System.currentTimeMillis() - index,
        )
    }

    private fun resolveProductImageUrl(productDto: ProductDto): String =
        appendImageVersion(
            normalizeImageUrl(
                productDto.imageUrl
                    ?: productDto.image
                    ?: productDto.images.asArrayOrNull()?.firstOrNull()?.asStringOrNull()
                    ?: "",
            ),
            productDto.updatedAt,
        )

    private fun normalizeImageUrl(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return trimmed
        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            val imageUri = Uri.parse(trimmed)
            val serverUri = Uri.parse(serverBaseUrl)
            val host = imageUri.host.orEmpty()
            if (host.equals("localhost", ignoreCase = true) ||
                host == "127.0.0.1" ||
                host == "10.0.2.2" ||
                host == "10.0.3.2"
            ) {
                val rebuilt = imageUri.buildUpon()
                    .scheme(serverUri.scheme)
                    .encodedAuthority(serverUri.encodedAuthority)
                    .build()
                    .toString()
                Log.d(TAG, "Rewriting product image host from $trimmed to $rebuilt")
                return rebuilt
            }
            return trimmed
        }
        return when {
            trimmed.startsWith("/") -> serverBaseUrl + trimmed
            else -> "$serverBaseUrl/$trimmed"
        }
    }

    private fun appendImageVersion(
        imageUrl: String,
        updatedAt: String?,
    ): String {
        if (imageUrl.isBlank()) return imageUrl
        val version = updatedAt?.trim().orEmpty().ifBlank { return imageUrl }
        val separator = if (imageUrl.contains("?")) "&" else "?"
        return "$imageUrl${separator}v=${version.hashCode()}"
    }

    private fun ReviewDto.toDomainReview(): Review = Review(
        id = id.asStringOrNull().orEmpty().ifBlank { createdAt.orEmpty() },
        authorName = authorName.orEmpty().ifBlank { "FLORA Collector" },
        rating = rating.asStringOrNull()?.toIntOrNull() ?: 0,
        text = text.orEmpty(),
        isVerified = isVerified ?: true,
        date = createdAt.orEmpty(),
    )

    private data class FavoritePayload(
        val productIds: Set<String>,
        val products: List<ProductDto>,
    )
}
