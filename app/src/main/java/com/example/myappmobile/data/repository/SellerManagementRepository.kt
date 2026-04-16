package com.example.myappmobile.data.repository

import android.util.Log
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.local.dummy.DummyOrders
import com.example.myappmobile.data.local.room.DatabaseProvider
import com.example.myappmobile.data.local.room.entity.ProductEntity
import com.example.myappmobile.domain.model.Address
import com.example.myappmobile.domain.model.Order
import com.example.myappmobile.domain.model.OrderItem
import com.example.myappmobile.domain.model.OrderStatus
import com.example.myappmobile.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SellerManagementRepository {

    private val productDao by lazy { DatabaseProvider.getDatabase().productDao() }

    fun getProductsForSeller(sellerId: String): Flow<List<Product>> = productDao.getBySellerId(
        normalizeSellerId(sellerId),
    ).map { entities ->
        entities.map(::toSellerProduct)
    }

    suspend fun upsertProduct(
        sellerId: String,
        productId: String?,
        name: String,
        price: Double,
        category: String,
        description: String,
        imageUrl: String,
        stockCount: Int,
    ) {
        val normalizedSellerId = normalizeSellerId(sellerId)
        val existing = productId?.let { productDao.getById(it) }
        val storeConfiguration = AppContainer.uiPreferencesRepository.getStoreConfiguration(normalizedSellerId)
        val product = ProductEntity(
            id = productId ?: "seller_${normalizedSellerId}_${System.currentTimeMillis()}",
            sellerId = normalizedSellerId,
            name = name,
            description = description,
            price = price,
            imageUrl = imageUrl.ifBlank { DEFAULT_PRODUCT_IMAGE },
            category = category,
            studio = storeConfiguration.shopName.ifBlank {
                existing?.studio?.ifBlank { DEFAULT_STUDIO } ?: DEFAULT_STUDIO
            },
            stockCount = stockCount,
            isFavorited = existing?.isFavorited ?: false,
            isFeatured = existing?.isFeatured ?: false,
            isNewArrival = existing?.isNewArrival ?: productId == null,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
        )
        productDao.insert(product)
    }

    suspend fun deleteProduct(sellerId: String, productId: String) {
        val product = productDao.getById(productId) ?: return
        if (product.sellerId != normalizeSellerId(sellerId)) return
        productDao.deleteById(productId)
    }

    fun getOrdersForSeller(sellerId: String): Flow<List<Order>> = productDao.getBySellerId(
        normalizeSellerId(sellerId),
    ).map { entities ->
        val sellerProducts = entities.map(::toSellerProduct).associateBy(Product::id)
        logDebug(sellerProducts.keys.toString())
        seedOrders(sellerProducts)
    }

    private fun seedOrders(sellerProducts: Map<String, Product>): List<Order> = listOfNotNull(
        createSeedOrder(
            id = "seller_order_1",
            reference = "FLR-42018",
            customerId = "u1",
            customerName = "Julian Thorne",
            customerLocation = "New York, United States",
            status = OrderStatus.PENDING,
            subtotal = 575.0,
            shippingCost = 18.0,
            tax = 46.0,
            total = 639.0,
            shippingMethod = "Premium Courier",
            placedDate = "Apr 1, 2026",
            items = listOf(
                seedOrderItem("seller_item_1", preferredProductIds = listOf("flora_seed_15", "sp1"), sellerProducts = sellerProducts, quantity = 2, variant = "Matte Charcoal"),
                seedOrderItem("seller_item_2", preferredProductIds = listOf("flora_seed_16", "sp3"), sellerProducts = sellerProducts, quantity = 1, variant = "Amberwood"),
            ),
        ),
        createSeedOrder(
            id = "seller_order_5",
            reference = "FLR-41863",
            customerName = DummyOrders.orderDetail.customerName,
            customerLocation = DummyOrders.orderDetail.customerLocation,
            status = OrderStatus.CANCELLED,
            subtotal = DummyOrders.orderDetail.subtotal,
            shippingCost = DummyOrders.orderDetail.shippingCost,
            tax = DummyOrders.orderDetail.tax,
            total = DummyOrders.orderDetail.total,
            shippingMethod = DummyOrders.orderDetail.shippingMethod,
            shippingAddress = DummyOrders.orderDetail.shippingAddress,
            placedDate = "Mar 14, 2026",
            estimatedDelivery = DummyOrders.orderDetail.estimatedDelivery,
            artisanPackaging = DummyOrders.orderDetail.artisanPackaging,
            items = listOf(
                seedOrderItem(
                    id = "seller_item_7",
                    preferredProductIds = listOf("flora_seed_15", "flora_seed_10", "sp7"),
                    sellerProducts = sellerProducts,
                    quantity = 1,
                    variant = "Sand Finish",
                ),
            ),
        ),
    )

    private fun seedOrderItem(
        id: String,
        preferredProductIds: List<String>,
        sellerProducts: Map<String, Product>,
        quantity: Int,
        variant: String,
    ): OrderItem? {
        val product = preferredProductIds.firstNotNullOfOrNull { sellerProducts[it] }
        if (product == null) {
            logWarning("Skipping seeded order item $id. Missing product ids: $preferredProductIds")
            return null
        }

        return OrderItem(
            id = id,
            product = product,
            quantity = quantity,
            variant = variant,
        )
    }

    private fun createSeedOrder(
        id: String,
        reference: String,
        customerId: String = "",
        customerName: String = "",
        customerLocation: String = "",
        items: List<OrderItem?>,
        status: OrderStatus,
        total: Double,
        shippingCost: Double,
        tax: Double,
        subtotal: Double,
        shippingMethod: String,
        shippingAddress: Address? = null,
        placedDate: String,
        estimatedDelivery: String = "",
        artisanPackaging: String = "Complimentary",
    ): Order? {
        val validItems = items.filterNotNull()
        if (validItems.isEmpty()) {
            logWarning("Skipping seeded order $id because no valid order items were created.")
            return null
        }

        return Order(
            id = id,
            reference = reference,
            customerId = customerId,
            customerName = customerName,
            customerLocation = customerLocation,
            items = validItems,
            status = status,
            total = total,
            shippingCost = shippingCost,
            tax = tax,
            subtotal = subtotal,
            shippingMethod = shippingMethod,
            shippingAddress = shippingAddress,
            placedDate = placedDate,
            estimatedDelivery = estimatedDelivery,
            artisanPackaging = artisanPackaging,
        )
    }

    private fun toSellerProduct(entity: ProductEntity): Product = Product(
        id = entity.id,
        name = entity.name,
        price = entity.price,
        imageUrl = entity.imageUrl,
        studio = entity.studio.ifBlank { DEFAULT_STUDIO },
        storeId = entity.sellerId,
        category = entity.category,
        description = entity.description,
        stockCount = entity.stockCount,
        isFavorited = entity.isFavorited,
        collectionLabel = entity.category.uppercase(),
        story = entity.description,
        images = listOf(entity.imageUrl),
    )

    private fun normalizeSellerId(sellerId: String): String = when (sellerId) {
        "s1" -> "1"
        else -> sellerId
    }

    private fun logDebug(message: String) {
        runCatching {
            Log.d(DEBUG_PRODUCTS_TAG, message)
        }
    }

    private fun logWarning(message: String) {
        runCatching {
            Log.w(TAG, message)
        }
    }

    private companion object {
        const val TAG = "SellerManagementRepo"
        const val DEBUG_PRODUCTS_TAG = "DEBUG_PRODUCTS"
        const val DEFAULT_STUDIO = "FLORA Ceramics"
        const val DEFAULT_PRODUCT_IMAGE =
            "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800"
    }
}
