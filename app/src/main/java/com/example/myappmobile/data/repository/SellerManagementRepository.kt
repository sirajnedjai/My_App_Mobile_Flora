package com.example.myappmobile.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.myappmobile.BuildConfig
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.local.room.DatabaseProvider
import com.example.myappmobile.data.local.room.entity.ProductEntity
import com.example.myappmobile.data.remote.ProductApiService
import com.example.myappmobile.data.remote.ProductDto
import com.example.myappmobile.data.remote.SellerProductApiService
import com.example.myappmobile.data.remote.SellerProductUpsertPayload
import com.example.myappmobile.data.remote.arrayAt
import com.example.myappmobile.data.remote.asArrayOrNull
import com.example.myappmobile.data.remote.asDoubleOrNull
import com.example.myappmobile.data.remote.asIntOrNull
import com.example.myappmobile.data.remote.asObjectOrNull
import com.example.myappmobile.data.remote.asStringOrNull
import com.example.myappmobile.data.remote.element
import com.example.myappmobile.data.remote.extractDataElement
import com.example.myappmobile.data.remote.objectAt
import com.example.myappmobile.data.remote.requireBody
import com.example.myappmobile.data.remote.string
import com.example.myappmobile.domain.model.Product
import com.example.myappmobile.domain.model.SellerManagedProductDetails
import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class SellerManagementRepository(
    private val productApiService: ProductApiService,
    private val sellerProductApiService: SellerProductApiService,
    private val gson: Gson,
) {

    private val productDao by lazy { DatabaseProvider.getDatabase().productDao() }
    private val _sellerProductsBySeller = MutableStateFlow<Map<String, List<Product>>>(emptyMap())
    private val serverBaseUrl = BuildConfig.FLORA_API_BASE_URL.removeSuffix("/api/")

    fun getProductsForSeller(sellerId: String): Flow<List<Product>> = _sellerProductsBySeller.map { cached ->
        cached[normalizeSellerId(sellerId)].orEmpty()
    }

    suspend fun refreshProductsForSeller(sellerId: String): Result<Unit> = runCatching {
        val normalizedSellerId = normalizeSellerId(sellerId)
        Log.d(TAG, "Fetching seller products for $normalizedSellerId")
        val response = productApiService.filterProducts(sellerId = normalizedSellerId).requireBody(gson)
        val products = parseSellerProducts(response.data, normalizedSellerId)
        _sellerProductsBySeller.update { current -> current + (normalizedSellerId to products) }
        syncSellerProductsToCache(normalizedSellerId, products, pruneMissing = true)
        Log.d(
            TAG,
            "Fetched ${products.size} seller products for $normalizedSellerId images=${products.joinToString { "${it.id}:${it.imageUrl}" }}",
        )
    }

    suspend fun fetchProductDetailsForSeller(
        sellerId: String,
        productId: String,
    ): Result<SellerManagedProductDetails> = runCatching {
        val normalizedSellerId = normalizeSellerId(sellerId)
        require(productId.isNotBlank()) { "Product id is missing." }
        Log.d(TAG, "Fetching seller product details. seller=$normalizedSellerId productId=$productId")
        val response = productApiService.getProductDetails(productId).requireBody(gson)
        val details = parseSellerProductDetails(response.data, normalizedSellerId)
            ?: throw IllegalStateException(response.message ?: "Product details are unavailable.")
        if (details.product.storeId.isNotBlank() && normalizeSellerId(details.product.storeId) != normalizedSellerId) {
            throw IllegalAccessException("You can view only your own products.")
        }
        val current = _sellerProductsBySeller.value[normalizedSellerId].orEmpty()
        _sellerProductsBySeller.update { cached ->
            cached + (normalizedSellerId to (listOf(details.product) + current.filterNot { it.id == details.product.id }))
        }
        syncSellerProductsToCache(normalizedSellerId, listOf(details.product), pruneMissing = false)
        Log.d(TAG, "Fetched seller product details successfully. productId=${details.product.id}")
        details
    }

    suspend fun upsertProduct(
        sellerId: String,
        productId: String?,
        name: String,
        price: Double,
        category: String,
        description: String,
        status: String,
        imageUri: String?,
        stockCount: Int,
    ): Result<Unit> = runCatching {
        val normalizedSellerId = normalizeSellerId(sellerId)
        val isUpdate = !productId.isNullOrBlank()
        val payload = SellerProductUpsertPayload(
            name = name,
            description = description,
            price = price,
            category = category,
            stock = stockCount,
            status = status,
            imageUri = imageUri,
        )
        val partMap = payload.toPartMap(includeMethodOverride = isUpdate)
        val imagePart = buildImagePart(AppContainer.applicationContext, payload.imageUri)
        Log.d(
            TAG,
            "Saving seller product. operation=${if (isUpdate) "update" else "create"} id=${productId.orEmpty()} seller=$normalizedSellerId selectedImageUri=${payload.imageUri.orEmpty()} payload=${gson.toJson(payload.copy(imageUri = payload.imageUri?.substringAfterLast('/')))} fields=${partMap.keys.sorted()} imageSelected=${imagePart != null}",
        )
        val response = if (!isUpdate) {
            sellerProductApiService.createProduct(partMap, imagePart).requireBody(gson)
        } else {
            sellerProductApiService.updateProduct(
                productId = requireNotNull(productId),
                methodOverride = "PUT",
                body = partMap,
                imageFile = imagePart,
            ).requireBody(gson)
        }
        val updatedDetails = parseSellerProductDetails(response.data, normalizedSellerId)
        Log.d(
            TAG,
            "Seller product save succeeded. operation=${if (isUpdate) "update" else "create"} id=${productId.orEmpty()} seller=$normalizedSellerId message=${response.message} updatedImageUrl=${updatedDetails?.product?.imageUrl.orEmpty()} data=${gson.toJson(response.data)}",
        )
        updatedDetails?.product?.let { updatedProduct ->
            _sellerProductsBySeller.update { current ->
                val existing = current[normalizedSellerId].orEmpty()
                current + (normalizedSellerId to (listOf(updatedProduct) + existing.filterNot { it.id == updatedProduct.id }))
            }
            syncSellerProductsToCache(normalizedSellerId, listOf(updatedProduct), pruneMissing = false)
            Log.d(TAG, "Seller product cache updated from save response. productId=${updatedProduct.id}")
        }
        refreshProductsForSeller(normalizedSellerId).onFailure { error ->
            Log.d(
                TAG,
                "Seller product save refresh failed after successful response. seller=$normalizedSellerId id=${productId.orEmpty()} error=${error.message}",
            )
        }
    }

    suspend fun deleteProduct(sellerId: String, productId: String): Result<Unit> = runCatching {
        val normalizedSellerId = normalizeSellerId(sellerId)
        Log.d(TAG, "Deleting seller product $productId for $normalizedSellerId")
        val response = sellerProductApiService.deleteProduct(productId).requireBody(gson)
        Log.d(TAG, "Seller product delete succeeded. message=${response.message} productId=$productId")
        productDao.deleteById(productId)
        _sellerProductsBySeller.update { current ->
            current + (normalizedSellerId to current[normalizedSellerId].orEmpty().filterNot { it.id == productId })
        }
        refreshProductsForSeller(normalizedSellerId).getOrThrow()
    }

    private fun parseSellerProducts(
        data: JsonElement?,
        sellerId: String,
    ): List<Product> {
        val payload = extractDataElement(data)
        val productElements = when {
            payload == null || payload.isJsonNull -> emptyList()
            payload.isJsonArray -> payload.asJsonArray.toList()
            payload.asObjectOrNull() != null -> payload.asObjectOrNull()
                ?.arrayAt("products", "data", "items")
                ?.toList()
                .orEmpty()
            else -> emptyList()
        }
        return productElements.mapIndexedNotNull { index, element ->
            runCatching { element.toSellerProduct(sellerId, index) }
                .onFailure { error -> Log.d(TAG, "Skipping malformed seller product: ${error.message}") }
                .getOrNull()
        }
    }

    private fun JsonElement.toSellerProduct(
        defaultSellerId: String,
        index: Int = 0,
    ): Product? {
        val dto = gson.fromJson(this, ProductDto::class.java)
        val obj = asObjectOrNull() ?: return null
        val categoryObject = dto.category.asObjectOrNull()
        val storeObject = dto.store.asObjectOrNull() ?: dto.seller.asObjectOrNull()
        val resolvedSellerId = normalizeSellerId(
            dto.storeId.asStringOrNull().orEmpty()
                .ifBlank { storeObject?.string("id", "_id", "seller_id", "store_id").orEmpty() }
                .ifBlank { defaultSellerId },
        )
        val studioName = storeObject?.string("name", "store_name", "shop_name", "seller_name")
            ?: AppContainer.uiPreferencesRepository.getStoreConfiguration(resolvedSellerId).shopName.ifBlank { DEFAULT_STUDIO }
        val resolvedId = dto.id.asStringOrNull().orEmpty()
            .ifBlank { obj.string("id", "_id", "product_id").orEmpty() }
            .ifBlank { "seller_${defaultSellerId.ifBlank { "unknown" }}_$index" }
        val resolvedImageUrl = resolveProductImageUrl(dto, obj)
        Log.d(
            TAG,
            "Seller product mapped. id=$resolvedId title=${dto.name.orEmpty()} rawImage=${dto.image} imageUrl=${dto.imageUrl} finalImage=$resolvedImageUrl",
        )
        return Product(
            id = resolvedId,
            name = dto.name.orEmpty().ifBlank { obj.string("name", "title") ?: "FLORA Piece" },
            price = dto.price.asDoubleOrNull()
                ?: obj.element("price", "amount", "sale_price").asDoubleOrNull()
                ?: 0.0,
            imageUrl = resolvedImageUrl,
            studio = studioName,
            storeId = resolvedSellerId,
            category = categoryObject?.string("name", "title")
                ?: dto.category.asStringOrNull().orEmpty(),
            description = dto.description.orEmpty(),
            stockCount = dto.stock.asIntOrNull()
                ?: obj.element("stock", "stock_count", "quantity").asIntOrNull()
                ?: 0,
            isFavorited = dto.isFavorite == true,
            collectionLabel = (categoryObject?.string("name", "title")
                ?: dto.category.asStringOrNull().orEmpty()).uppercase(),
            story = dto.description.orEmpty(),
            images = buildList {
                add(resolvedImageUrl)
                dto.images.asArrayOrNull()?.forEach { image -> add(normalizeImageUrl(image.asStringOrNull().orEmpty())) }
            }.filter { it.isNotBlank() }.distinct(),
            status = dto.status.orEmpty().ifBlank { obj.string("status").orEmpty() },
        )
    }

    private fun parseSellerProductDetails(
        data: JsonElement?,
        sellerId: String,
    ): SellerManagedProductDetails? {
        val payload = extractDataElement(data) ?: return null
        val productObject = when {
            payload.asObjectOrNull() != null -> payload.asObjectOrNull()
            else -> null
        } ?: return null
        val product = productObject.toSellerProduct(sellerId) ?: return null
        Log.d(
            TAG,
            "Seller product details parsed. id=${product.id} imageUrl=${product.imageUrl} updatedAt=${productObject.string("updated_at").orEmpty()}",
        )
        return SellerManagedProductDetails(
            product = product,
            status = productObject.string("status").orEmpty().ifBlank { product.status },
            createdAt = productObject.string("created_at").orEmpty(),
            updatedAt = productObject.string("updated_at").orEmpty(),
            sellerName = productObject.objectAt("seller", "store")?.string("name", "store_name", "shop_name", "seller_name")
                ?: product.studio,
        )
    }

    private suspend fun syncSellerProductsToCache(
        sellerId: String,
        products: List<Product>,
        pruneMissing: Boolean,
    ) {
        val currentProducts = productDao.getAllOnce().filter { it.sellerId == sellerId }
        val existingById = currentProducts.associateBy(ProductEntity::id)
        if (pruneMissing) {
            val remoteIds = products.map(Product::id).toSet()
            currentProducts.filter { it.id !in remoteIds }.forEach { entity ->
                productDao.deleteById(entity.id)
            }
        }
        if (products.isEmpty()) return
        productDao.insertAll(
            products.map { product ->
                val existing = existingById[product.id]
                ProductEntity(
                    id = product.id,
                    sellerId = sellerId,
                    name = product.name,
                    description = product.description,
                    price = product.price,
                    imageUrl = product.imageUrl.ifBlank { DEFAULT_PRODUCT_IMAGE },
                    category = product.category,
                    studio = product.studio.ifBlank { DEFAULT_STUDIO },
                    stockCount = product.stockCount,
                    isFavorited = existing?.isFavorited ?: product.isFavorited,
                    isFeatured = existing?.isFeatured ?: false,
                    isNewArrival = existing?.isNewArrival ?: false,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                )
            },
        )
    }

    private fun normalizeSellerId(sellerId: String): String = when (sellerId) {
        "s1" -> "1"
        else -> sellerId
    }

    private fun normalizeImageUrl(raw: String): String {
        val value = raw.trim()
        if (value.isBlank()) return value
        if (value.startsWith("http://") || value.startsWith("https://")) {
            val imageUri = Uri.parse(value)
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
                Log.d(TAG, "Rewriting seller product image host from $value to $rebuilt")
                return rebuilt
            }
            return value
        }
        return if (value.startsWith("/")) "$serverBaseUrl$value" else "$serverBaseUrl/$value"
    }

    private fun resolveProductImageUrl(
        productDto: ProductDto,
        obj: com.google.gson.JsonObject,
    ): String = appendImageVersion(
        normalizeImageUrl(
            productDto.imageUrl
                ?: productDto.image
                ?: obj.string("image_url", "image", "thumbnail", "photo")
                ?: productDto.images.asArrayOrNull()?.firstOrNull()?.asStringOrNull()
                ?: DEFAULT_PRODUCT_IMAGE,
        ),
        productDto.updatedAt ?: obj.string("updated_at"),
    )

    private fun appendImageVersion(
        imageUrl: String,
        updatedAt: String?,
    ): String {
        if (imageUrl.isBlank()) return imageUrl
        val version = updatedAt?.trim().orEmpty().ifBlank { return imageUrl }
        val separator = if (imageUrl.contains("?")) "&" else "?"
        return "$imageUrl${separator}v=${version.hashCode()}"
    }

    private fun SellerProductUpsertPayload.toPartMap(
        includeMethodOverride: Boolean = false,
    ): Map<String, RequestBody> = buildMap {
        put("name", name.toPlainTextRequestBody())
        put("description", description.toPlainTextRequestBody())
        put("price", price.toString().toPlainTextRequestBody())
        put("category", category.toPlainTextRequestBody())
        put("stock", stock.toString().toPlainTextRequestBody())
        put("status", status.toPlainTextRequestBody())
        if (includeMethodOverride) {
            put("_method", "PUT".toPlainTextRequestBody())
        }
    }

    private fun buildImagePart(
        context: Context,
        imageUri: String?,
    ): MultipartBody.Part? {
        val uriString = imageUri?.trim().orEmpty()
        if (uriString.isBlank()) return null
        val uri = Uri.parse(uriString)
        val mimeType = context.contentResolver.getType(uri)?.takeIf { it.isNotBlank() } ?: "application/octet-stream"
        val fileName = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
            }
            ?: uri.lastPathSegment?.substringAfterLast('/')?.ifBlank { null }
            ?: "product_image"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Unable to read the selected image.")
        Log.d(TAG, "Preparing product image upload. uri=$uri mimeType=$mimeType fileName=$fileName sizeBytes=${bytes.size}")
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("image_file", fileName, requestBody)
    }

    private fun String.toPlainTextRequestBody(): RequestBody = toRequestBody("text/plain".toMediaTypeOrNull())

    private companion object {
        const val TAG = "SellerManagementRepo"
        const val DEFAULT_STUDIO = "FLORA Ceramics"
        const val DEFAULT_PRODUCT_IMAGE = ""
    }
}
