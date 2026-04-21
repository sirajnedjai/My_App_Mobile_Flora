package com.example.myappmobile.data.repository

import android.net.Uri
import android.util.Log
import com.example.myappmobile.BuildConfig
import com.example.myappmobile.data.mapper.ProductMapper
import com.example.myappmobile.data.remote.AddToCartRequestDto
import com.example.myappmobile.data.remote.ApiException
import com.example.myappmobile.data.remote.CartApiService
import com.example.myappmobile.data.remote.CartDto
import com.example.myappmobile.data.remote.CartItemDto
import com.example.myappmobile.data.remote.ProductDto
import com.example.myappmobile.data.remote.UpdateCartItemRequestDto
import com.example.myappmobile.data.remote.arrayAt
import com.example.myappmobile.data.remote.asObjectOrNull
import com.example.myappmobile.data.remote.asStringOrNull
import com.example.myappmobile.data.remote.extractDataElement
import com.example.myappmobile.data.remote.requireBody
import com.example.myappmobile.data.remote.string
import com.example.myappmobile.data.remote.toApiException
import com.example.myappmobile.domain.Product
import com.example.myappmobile.domain.model.CartItem
import com.example.myappmobile.domain.repository.AuthRepository
import com.example.myappmobile.domain.repository.CartRepository
import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CartRepositoryImpl(
    private val cartApiService: CartApiService,
    private val authRepository: AuthRepository,
    private val gson: Gson,
) : CartRepository {
    private companion object {
        const val TAG = "CartRepository"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val serverBaseUrl = BuildConfig.FLORA_API_BASE_URL.removeSuffix("/api/")
    private val _cartItems = MutableStateFlow(emptyList<CartItem>())
    override val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()
    private val _cartId = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        scope.launch {
            authRepository.currentUser.collectLatest { user ->
                if (user.isAuthenticated) {
                    refreshCart()
                } else {
                    _cartId.value = null
                    _cartItems.value = emptyList()
                    _statusMessage.value = null
                }
            }
        }
    }

    override suspend fun addToCart(product: Product) {
        if (!requireAuthenticatedSession()) return

        Log.d(TAG, "Adding product to cart. productId=${product.id}")
        runCatching {
            val response = cartApiService.addToCart(
                AddToCartRequestDto(
                    productId = product.id,
                    quantity = 1,
                ),
            ).requireBody(gson)
            _statusMessage.value = response.message ?: "Product added to cart successfully."
            refreshCart()
        }.onFailure { throwable ->
            Log.d(TAG, "Add to cart failed. productId=${product.id} error=${throwable.toApiException().message}")
            _statusMessage.value = throwable.toApiException().message
        }
    }

    override suspend fun increaseQuantity(productId: String) {
        updateQuantity(productId = productId, delta = 1)
    }

    override suspend fun decreaseQuantity(productId: String) {
        updateQuantity(productId = productId, delta = -1)
    }

    override suspend fun removeFromCart(productId: String) {
        if (!requireAuthenticatedSession()) return

        val item = cartItems.value.firstOrNull { it.product.id == productId }
            ?: run {
                _statusMessage.value = "Cart item not found or access denied."
                return
            }

        runCatching {
            val response = cartApiService.deleteCartItem(item.id).requireBody(gson)
            _statusMessage.value = response.message ?: "Cart item removed successfully."
            refreshCart()
        }.onFailure { throwable ->
            Log.d(TAG, "Remove from cart failed. itemId=${item.id} error=${throwable.toApiException().message}")
            _statusMessage.value = throwable.toApiException().message
        }
    }

    override suspend fun clearCart() {
        val currentItems = cartItems.value
        if (currentItems.isEmpty()) {
            _cartId.value = null
            _cartItems.value = emptyList()
            return
        }
        if (!authRepository.currentUser.value.isAuthenticated) {
            _cartId.value = null
            _cartItems.value = emptyList()
            return
        }

        currentItems.forEach { item ->
            runCatching {
                cartApiService.deleteCartItem(item.id).requireBody(gson)
            }
        }
        refreshCart()
    }

    override fun getCheckoutItems(): List<CartItem> = cartItems.value
    override fun getCheckoutCartId(): String? = _cartId.value

    private suspend fun updateQuantity(productId: String, delta: Int) {
        if (!requireAuthenticatedSession()) return

        val currentItem = cartItems.value.firstOrNull { it.product.id == productId }
            ?: run {
                _statusMessage.value = "Cart item not found or access denied."
                return
            }

        val newQuantity = (currentItem.quantity + delta).coerceAtLeast(1)
        Log.d(TAG, "Updating cart item. itemId=${currentItem.id} productId=$productId quantity=$newQuantity")
        runCatching {
            val response = cartApiService.updateCartItem(
                itemId = currentItem.id,
                body = UpdateCartItemRequestDto(quantity = newQuantity),
            ).requireBody(gson)
            _statusMessage.value = response.message ?: "Cart item updated successfully."
            refreshCart()
        }.onFailure { throwable ->
            Log.d(TAG, "Update cart item failed. itemId=${currentItem.id} error=${throwable.toApiException().message}")
            _statusMessage.value = throwable.toApiException().message
        }
    }

    private suspend fun refreshCart() {
        if (!authRepository.currentUser.value.isAuthenticated) {
            _cartId.value = null
            _cartItems.value = emptyList()
            return
        }

        _isLoading.value = true
        runCatching {
            val response = cartApiService.getCart().requireBody(gson)
            val parsedCart = extractCart(response.data)
            _cartId.value = parsedCart.first
            _cartItems.value = parsedCart.second
            Log.d(TAG, "Cart fetched successfully. items=${_cartItems.value.size}")
            if (_cartItems.value.isEmpty()) {
                _statusMessage.value = response.message?.takeIf { it.isNotBlank() }
            }
        }.onFailure { throwable ->
            val apiError = throwable.toApiException()
            Log.d(TAG, "Cart fetch failed. error=${apiError.message}")
            if (apiError.statusCode == 401) {
                _cartItems.value = emptyList()
            }
            _statusMessage.value = apiError.message
        }.also {
            _isLoading.value = false
        }
    }

    private fun extractCart(data: JsonElement?): Pair<String?, List<CartItem>> {
        val payload = extractDataElement(data)
        val cartId = when {
            payload?.asObjectOrNull() != null -> {
                val obj = payload.asObjectOrNull()
                val dto = runCatching { gson.fromJson(payload, CartDto::class.java) }.getOrNull()
                dto?.id?.asStringOrNull() ?: obj?.string("id", "cart_id")
            }
            else -> null
        }
        val items = when {
            payload == null || payload.isJsonNull -> emptyList()
            payload.isJsonArray -> payload.asJsonArray.mapNotNull { element ->
                runCatching { gson.fromJson(element, CartItemDto::class.java) }.getOrNull()
            }
            payload.asObjectOrNull() != null -> {
                val obj = payload.asObjectOrNull()
                val dto = runCatching { gson.fromJson(payload, CartDto::class.java) }.getOrNull()
                dto?.items
                    ?: dto?.cartItems
                    ?: obj?.arrayAt("items", "cart_items", "data")?.mapNotNull { element ->
                        runCatching { gson.fromJson(element, CartItemDto::class.java) }.getOrNull()
                    }
                    .orEmpty()
            }
            else -> emptyList()
        }

        return cartId to items.mapNotNull { dto -> dto.toDomainCartItem() }
    }

    private fun CartItemDto.toDomainCartItem(): CartItem? {
        val productPayload = product
        val productDto = productPayload?.let {
            runCatching { gson.fromJson(it, ProductDto::class.java) }.getOrNull()
        }
        val productJson = productPayload?.asObjectOrNull()
        val productId = productDto?.id?.asStringOrNull()
            ?: productId.asStringOrNull()
            ?: productJson?.string("id", "_id", "product_id")
            ?: return null
        val resolvedImageUrl = normalizeImageUrl(
            productDto?.imageUrl
            ?: productDto?.image
            ?: productPayload?.asObjectOrNull()?.string("image_url", "image", "thumbnail", "photo")
            ?: productPayload?.asObjectOrNull()?.arrayAt("images")?.firstOrNull()?.asStringOrNull()
            ?: "",
        )
        Log.d(
            TAG,
            "Cart item product image mapping. productId=$productId title=${productDto?.name.orEmpty()} rawImage=${productDto?.image} imageUrl=${productDto?.imageUrl} finalImage=$resolvedImageUrl",
        )

        val domainProduct = Product(
            id = productId,
            name = productDto?.name.orEmpty().ifBlank { productJson?.string("name", "title") ?: "FLORA Piece" },
            price = productDto?.price?.asStringOrNull()?.toDoubleOrNull()
                ?: productJson?.string("price", "amount", "sale_price")?.toDoubleOrNull()
                ?: 0.0,
            imageUrl = resolvedImageUrl,
            studio = productJson?.asObjectOrNull()?.string("store_name", "shop_name", "studio", "seller_name")
                ?: productDto?.store?.asObjectOrNull()?.string("name", "store_name", "shop_name")
                ?: "FLORA Atelier",
            category = com.example.myappmobile.domain.Category(
                id = com.example.myappmobile.data.mapper.ProductEntityMapper.categoryId(
                    productDto?.category?.asObjectOrNull()?.string("name", "title")
                        ?: productDto?.category?.asStringOrNull().orEmpty(),
                ),
                name = productDto?.category?.asObjectOrNull()?.string("name", "title")
                    ?: productDto?.category?.asStringOrNull().orEmpty().ifBlank { "Curated" },
                iconRes = android.R.drawable.ic_menu_gallery,
            ),
            isFavorited = productDto?.isFavorite == true,
        )

        return CartItem(
            id = id.asStringOrNull().orEmpty().ifBlank { "cart_$productId" },
            product = ProductMapper.map(domainProduct),
            quantity = quantity.asStringOrNull()?.toIntOrNull() ?: 1,
            selectedVariant = variant.orEmpty(),
            selectedSize = size.orEmpty(),
        )
    }

    private fun requireAuthenticatedSession(): Boolean {
        if (authRepository.currentUser.value.isAuthenticated) {
            return true
        }
        _statusMessage.value = ApiException("Your session has expired. Please sign in again.", 401).message
        return false
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
                Log.d(TAG, "Rewriting cart image host from $value to $rebuilt")
                return rebuilt
            }
            return value
        }
        return if (value.startsWith("/")) "$serverBaseUrl$value" else "$serverBaseUrl/$value"
    }
}
