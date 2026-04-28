package com.example.myappmobile.data.repository

import android.util.Log
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.BackendUrlResolver
import com.example.myappmobile.data.remote.CheckoutRequestDto
import com.example.myappmobile.data.remote.OrderApiService
import com.example.myappmobile.data.remote.OrderDto
import com.example.myappmobile.data.remote.ProductDto
import com.example.myappmobile.data.remote.SellerOrderApiService
import com.example.myappmobile.data.remote.SellerOrderStatusUpdateRequestDto
import com.example.myappmobile.data.remote.arrayAt
import com.example.myappmobile.data.remote.asArrayOrNull
import com.example.myappmobile.data.remote.asDoubleOrNull
import com.example.myappmobile.data.remote.asIntOrNull
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
import com.example.myappmobile.domain.model.Address
import com.example.myappmobile.domain.model.CheckoutDraft
import com.example.myappmobile.domain.model.Order
import com.example.myappmobile.domain.model.OrderItem
import com.example.myappmobile.domain.model.OrderStatus
import com.example.myappmobile.domain.model.OrderStatusEntry
import com.example.myappmobile.domain.model.Product
import com.example.myappmobile.domain.repository.CartRepository
import com.example.myappmobile.domain.repository.OrderRepository
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrderRepositoryImpl(
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepositoryImpl,
    private val accountSettingsRepository: AccountSettingsRepository,
    private val orderApiService: OrderApiService,
    private val sellerOrderApiService: SellerOrderApiService,
    private val gson: Gson,
) : OrderRepository {
    private companion object {
        const val TAG = "OrderRepository"
    }

    private val timestampFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm")
    private val displayTimestampFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _checkoutDraft = MutableStateFlow(CheckoutDraft())
    override val checkoutDraft: StateFlow<CheckoutDraft> = _checkoutDraft.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    override val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _sellerOrdersBySeller = MutableStateFlow<Map<String, List<Order>>>(emptyMap())

    private var latestOrder: Order? = null

    init {
        scope.launch {
            authRepository.currentUser.collectLatest { user ->
                if (user.isAuthenticated) {
                    _checkoutDraft.value = defaultCheckoutDraft(user.id)
                    runCatching { refreshCustomerOrders(user.id) }
                        .onFailure { error -> Log.d(TAG, "Unable to refresh customer orders. error=${error.toApiException().message}") }
                } else {
                    _checkoutDraft.value = CheckoutDraft()
                    _orders.value = emptyList()
                    latestOrder = null
                }
            }
        }
    }

    override suspend fun updateAddress(address: Address) {
        _checkoutDraft.update { it.copy(address = address) }
    }

    override suspend fun updateShippingMethod(method: String) {
        _checkoutDraft.update { it.copy(shippingMethod = method) }
    }

    override suspend fun updatePaymentMethod(method: String) {
        _checkoutDraft.update { it.copy(paymentMethod = method) }
    }

    override suspend fun createOrder(): Order {
        val currentUser = authRepository.currentUser.value
        val items = cartRepository.getCheckoutItems()
        check(currentUser.isAuthenticated) { "Your session has expired. Please sign in again." }
        check(items.isNotEmpty()) { "Your cart is empty." }

        val checkoutAddress = _checkoutDraft.value.address ?: defaultCheckoutDraft(currentUser.id).address
        check(checkoutAddress != null) { "Complete the shipping address before placing the order." }
        val total = items.sumOf { it.product.price * it.quantity } +
            when (_checkoutDraft.value.shippingMethod) {
                "office_pickup" -> 150.0
                else -> 300.0
            }

        val request = CheckoutRequestDto(
            fullName = checkoutAddress.fullName,
            phone = checkoutAddress.phoneNumber,
            country = checkoutAddress.country,
            state = checkoutAddress.state,
            municipality = checkoutAddress.municipality,
            neighborhood = checkoutAddress.neighborhood,
            streetAddress = checkoutAddress.street,
            notes = null,
            shippingMethod = _checkoutDraft.value.shippingMethod,
            paymentMethod = _checkoutDraft.value.paymentMethod,
        )

        Log.d(
            TAG,
            "Checkout request shipping/payment mapping: shipping='${_checkoutDraft.value.shippingMethod}' payment='${_checkoutDraft.value.paymentMethod}' localPreviewTotal=$total",
        )
        Log.d(TAG, "Checkout request body: ${gson.toJson(request)}")
        val response = runCatching {
            orderApiService.checkout(request).requireBody(gson)
        }.getOrElse { error ->
            val apiError = error.toApiException()
            Log.d(TAG, "Checkout API failed. message=${apiError.message} validation=${apiError.validationErrors}")
            throw apiError
        }
        Log.d(TAG, "Checkout response received. message=${response.message.orEmpty()}")

        val responseOrder = parseCheckoutOrder(response.data)
        refreshCustomerOrders(currentUser.id)
        val order = responseOrder
            ?: latestOrder
            ?: _orders.value.firstOrNull()
            ?: throw IllegalStateException(response.message ?: "Checkout succeeded but no order details were returned.")

        latestOrder = order
        order.items
            .map { it.product.storeId }
            .filter { it.isNotBlank() }
            .distinct()
            .forEach { sellerId ->
                AppContainer.notificationService.sendSellerOrderPlacedNotification(
                    sellerId = sellerId,
                    order = order,
                )
            }
        cartRepository.clearCart()
        return order
    }

    override suspend fun refreshCustomerOrders(): Result<Unit> = runCatching {
        val currentUser = authRepository.currentUser.value
        check(currentUser.isAuthenticated && !currentUser.isSeller) { "Sign in with a buyer account to view orders." }
        refreshCustomerOrders(currentUser.id)
    }

    override suspend fun fetchOrderDetails(orderId: String): Result<Order> = runCatching {
        val currentUser = authRepository.currentUser.value
        check(currentUser.isAuthenticated && !currentUser.isSeller) { "Sign in with a buyer account to view this order." }
        check(orderId.isNotBlank()) { "Order id is missing." }
        Log.d(TAG, "Fetching order details for orderId=$orderId")
        val response = orderApiService.getOrder(orderId).requireBody(gson)
        val order = parseCheckoutOrder(response.data)
            ?: throw IllegalStateException(response.message ?: "Order details are unavailable.")
        _orders.update { current ->
            val filtered = current.filterNot { it.id == order.id }
            (listOf(order) + filtered).sortedByDescending(Order::id)
        }
        if (latestOrder?.id == order.id || latestOrder == null) {
            latestOrder = order
        }
        Log.d(TAG, "Fetched order details successfully. orderId=${order.id} items=${order.items.size}")
        order
    }

    override suspend fun refreshSellerOrders(): Result<Unit> = runCatching {
        val currentUser = authRepository.currentUser.value
        check(currentUser.isAuthenticated && currentUser.isSeller) { "Sign in with a seller account to view received orders." }
        refreshSellerOrders(currentUser.id).getOrThrow()
    }

    override suspend fun fetchSellerOrderDetails(orderId: String): Result<Order> = runCatching {
        val currentUser = authRepository.currentUser.value
        check(currentUser.isAuthenticated && currentUser.isSeller) { "Sign in with a seller account to view this order." }
        check(orderId.isNotBlank()) { "Order id is missing." }
        val normalizedSellerId = normalizedSellerId(currentUser.id)
        Log.d(TAG, "Fetching seller order details for seller=$normalizedSellerId orderId=$orderId")
        val response = sellerOrderApiService.getSellerOrder(orderId).requireBody(gson)
        val order = parseCheckoutOrder(response.data, normalizedSellerId)
            ?.forSeller(normalizedSellerId)
            ?: throw IllegalStateException("The server did not return usable order details for this order.")
        _sellerOrdersBySeller.update { current ->
            val updated = current[normalizedSellerId].orEmpty().filterNot { it.id == order.id }
            current + (normalizedSellerId to (listOf(order) + updated).sortedByDescending(Order::id))
        }
        Log.d(TAG, "Fetched seller order details successfully. orderId=${order.id} items=${order.items.size}")
        order
    }

    override fun getRecentOrders(): List<Order> = orders.value

    override fun getOrdersForCustomer(customerId: String): List<Order> = orders.value
        .filter { order -> order.customerId == customerId }
        .sortedByDescending(Order::id)

    override fun observeOrdersForCustomer(customerId: String): Flow<List<Order>> = orders.map { allOrders ->
        allOrders.filter { it.customerId == customerId }.sortedByDescending(Order::id)
    }

    override fun getOrdersForSeller(sellerId: String): List<Order> {
        val normalizedSellerId = normalizedSellerId(sellerId)
        return _sellerOrdersBySeller.value[normalizedSellerId].orEmpty().ifEmpty {
            orders.value
                .filter { order -> order.containsSeller(normalizedSellerId) }
                .map { order -> order.forSeller(normalizedSellerId) }
                .sortedByDescending(Order::id)
        }
    }

    override fun observeOrdersForSeller(sellerId: String): Flow<List<Order>> = _sellerOrdersBySeller.map { cached ->
        val normalizedSellerId = normalizedSellerId(sellerId)
        cached[normalizedSellerId].orEmpty().ifEmpty {
            orders.value
                .filter { order -> order.containsSeller(normalizedSellerId) }
                .map { order -> order.forSeller(normalizedSellerId) }
                .sortedByDescending(Order::id)
        }
    }

    override fun getOrder(orderId: String): Order? = orders.value.firstOrNull { it.id == orderId }

    override fun getOrderForSeller(sellerId: String, orderId: String): Order? {
        val normalizedSellerId = normalizedSellerId(sellerId)
        return _sellerOrdersBySeller.value[normalizedSellerId]
            ?.firstOrNull { it.id == orderId }
            ?: getOrder(orderId)
                ?.takeIf { it.containsSeller(normalizedSellerId) }
                ?.forSeller(normalizedSellerId)
    }

    suspend fun refreshSellerOrders(sellerId: String): Result<Unit> = runCatching {
        val normalizedSellerId = normalizedSellerId(sellerId)
        Log.d(TAG, "Fetching seller orders for $normalizedSellerId")
        val response = sellerOrderApiService.getSellerOrders().requireBody(gson)
        val remoteOrders = parseSellerOrders(response.data, normalizedSellerId)
        _sellerOrdersBySeller.update { current -> current + (normalizedSellerId to remoteOrders) }
        Log.d(TAG, "Fetched ${remoteOrders.size} seller orders for $normalizedSellerId")
    }

    override suspend fun updateOrderStatus(
        orderId: String,
        sellerId: String,
        newStatus: OrderStatus,
    ): Result<Order> {
        val normalizedSellerId = normalizedSellerId(sellerId)
        val currentUser = authRepository.currentUser.value
        if (!currentUser.isAuthenticated || !currentUser.isSeller) {
            return Result.failure(IllegalAccessException("Sign in with a seller account to manage orders."))
        }

        val currentOrder = getOrderForSeller(normalizedSellerId, orderId)
            ?: fetchSellerOrderDetails(orderId).getOrElse { error -> return Result.failure(error) }

        if (currentOrder.status == newStatus) {
            return Result.success(currentOrder)
        }

        return runCatching {
            val backendStatus = toBackendSellerStatus(newStatus)
            Log.d(TAG, "Updating seller order status. seller=$normalizedSellerId orderId=$orderId status=$backendStatus")
            val response = sellerOrderApiService.updateSellerOrderStatus(
                orderId = orderId,
                body = SellerOrderStatusUpdateRequestDto(status = backendStatus),
            ).requireBody(gson)
            val responseOrder = parseCheckoutOrder(response.data, normalizedSellerId)
                ?.forSeller(normalizedSellerId)
            val refreshedOrder = fetchSellerOrderDetails(orderId).getOrNull()
            val fallbackPatchedOrder = currentOrder.withStatusUpdate(newStatus)
            val updatedOrder = listOfNotNull(refreshedOrder, responseOrder)
                .firstOrNull { it.status == newStatus }
                ?: refreshedOrder?.withStatusUpdate(newStatus)
                ?: responseOrder?.withStatusUpdate(newStatus)
                ?: fallbackPatchedOrder

            _sellerOrdersBySeller.update { current ->
                val updated = current[normalizedSellerId].orEmpty().filterNot { it.id == updatedOrder.id }
                current + (normalizedSellerId to (listOf(updatedOrder) + updated).sortedByDescending(Order::id))
            }
            _orders.update { current ->
                current.map { order ->
                    if (order.id == updatedOrder.id) {
                        order.mergeStatusFrom(updatedOrder)
                    } else {
                        order
                    }
                }
            }
            if (latestOrder?.id == updatedOrder.id) {
                latestOrder = latestOrder?.mergeStatusFrom(updatedOrder) ?: updatedOrder
            }

            if (currentOrder.status != OrderStatus.DELIVERED && updatedOrder.status == OrderStatus.DELIVERED) {
                AppContainer.notificationService.sendOrderDeliveredNotification(
                    userId = updatedOrder.customerId,
                    order = updatedOrder,
                )
            }

            Log.d(TAG, "Updated seller order status successfully. orderId=$orderId status=${updatedOrder.status}")
            updatedOrder
        }
    }

    override fun getLatestOrder(): Order? = latestOrder

    private suspend fun refreshCustomerOrders(customerId: String) {
        Log.d(TAG, "Fetching customer orders for $customerId")
        val response = orderApiService.getOrders().requireBody(gson)
        val remoteOrders = parseCustomerOrders(response.data)
        _orders.value = remoteOrders
        latestOrder = remoteOrders.firstOrNull()
        Log.d(TAG, "Fetched ${remoteOrders.size} customer orders for $customerId")
    }

    private fun parseSellerOrders(
        data: JsonElement?,
        sellerId: String,
    ): List<Order> = parseOrders(data, sellerId)

    private fun parseCustomerOrders(data: JsonElement?): List<Order> = parseOrders(data, "")

    private fun parseOrders(
        data: JsonElement?,
        defaultSellerId: String,
    ): List<Order> {
        val payload = extractDataElement(data)
        val orderElements = when {
            payload == null || payload.isJsonNull -> emptyList()
            payload.isJsonArray -> payload.asJsonArray.toList()
            payload.asObjectOrNull() != null -> payload.asObjectOrNull()
                ?.arrayAt("orders", "data", "items")
                ?.toList()
                .orEmpty()
            else -> emptyList()
        }
        return orderElements.mapIndexedNotNull { index, element ->
            runCatching { element.asObjectOrNull()?.toOrder(defaultSellerId, index) }
                .onFailure { error -> Log.d(TAG, "Skipping malformed order entry: ${error.message}") }
                .getOrNull()
        }
    }

    private fun parseCheckoutOrder(
        data: JsonElement?,
        defaultSellerId: String = "",
    ): Order? {
        if (data == null || data.isJsonNull) return null
        val extracted = extractDataElement(data)
        val rootObject = data.asObjectOrNull()
        val extractedObject = extracted?.asObjectOrNull()
        val candidateObjects = buildList {
            rootObject?.let(::add)
            extractedObject?.let(::add)
            rootObject?.objectAt("order", "seller_order", "order_details", "details")?.let(::add)
            extractedObject?.objectAt("order", "seller_order", "order_details", "details")?.let(::add)
            data.asArrayOrNull()?.firstOrNull()?.asObjectOrNull()?.let(::add)
            extracted?.asArrayOrNull()?.firstOrNull()?.asObjectOrNull()?.let(::add)
        }
            .distinctBy(JsonObject::toString)

        candidateObjects.forEachIndexed { index, candidate ->
            runCatching {
                candidate.toOrder(
                    defaultSellerId = defaultSellerId,
                    index = index,
                    supportObject = rootObject ?: extractedObject,
                )
            }.onSuccess { order ->
                if (order != null) {
                    Log.d(TAG, "Seller checkout order parsed. orderId=${order.id} items=${order.items.size}")
                    return order
                }
            }.onFailure { error ->
                Log.d(TAG, "Seller checkout order candidate failed: ${error.message}")
            }
        }

        Log.d(
            TAG,
            "Seller checkout order parsing returned null. rootKeys=${rootObject?.keySet()?.joinToString()} extractedKeys=${extractedObject?.keySet()?.joinToString()}",
        )
        return null
    }

    private fun JsonObject.toOrder(
        defaultSellerId: String,
        index: Int = 0,
        supportObject: JsonObject? = null,
    ): Order? {
        val summaryObject = objectAt("order", "seller_order", "order_details", "details")
            ?: supportObject?.objectAt("order", "seller_order", "order_details", "details")
        val dtoSource = summaryObject ?: this
        val dto = gson.fromJson(dtoSource, OrderDto::class.java)
        val items = parseOrderItems(this, defaultSellerId, supportObject)

        val shippingAddress = objectAt("shipping_address", "address", "delivery_address")
            ?.toAddress()
            ?: supportObject?.objectAt("shipping_address", "address", "delivery_address")?.toAddress()
        val customerObject = objectAt("customer", "buyer", "user")
            ?: supportObject?.objectAt("customer", "buyer", "user")
        val provisionalStatus = parseStatus(
            dto.status
                ?: string("status", "order_status", "shipping_status", "delivery_status", "tracking_status", "fulfillment_status")
                ?: supportObject?.string("status", "order_status", "shipping_status", "delivery_status", "tracking_status", "fulfillment_status")
                ?: summaryObject?.string("status", "order_status", "shipping_status", "delivery_status", "tracking_status", "fulfillment_status")
                .orEmpty(),
        )
        val statusHistory = parseStatusHistory(this, provisionalStatus, placedDate = "", supportObject = supportObject)
        val status = statusHistory.lastOrNull()?.status ?: provisionalStatus
        val subtotal = double("subtotal", "sub_total")
            ?: supportObject?.double("subtotal", "sub_total")
            ?: summaryObject?.double("subtotal", "sub_total")
            ?: items.sumOf { it.product.price * it.quantity }
        val shippingCost = double("shipping_cost", "shipping", "delivery_fee")
            ?: supportObject?.double("shipping_cost", "shipping", "delivery_fee")
            ?: summaryObject?.double("shipping_cost", "shipping", "delivery_fee")
            ?: 0.0
        val tax = double("tax", "tax_amount", "vat")
            ?: supportObject?.double("tax", "tax_amount", "vat")
            ?: summaryObject?.double("tax", "tax_amount", "vat")
            ?: 0.0
        val total = dto.total.asDoubleOrNull()
            ?: double("total", "grand_total")
            ?: supportObject?.double("total", "grand_total")
            ?: summaryObject?.double("total", "grand_total")
            ?: subtotal + shippingCost + tax
        val placedDate = formatOrderTimestamp(
            string("placed_date", "created_at", "date")
                ?: supportObject?.string("placed_date", "created_at", "date")
                ?: summaryObject?.string("placed_date", "created_at", "date")
                .orEmpty(),
        )
        val normalizedHistory = if (statusHistory.size == 1 && statusHistory.first().timestamp.isBlank()) {
            parseStatusHistory(this, provisionalStatus, placedDate, supportObject)
        } else {
            statusHistory.map { entry ->
                if (entry.timestamp.isBlank()) entry.copy(timestamp = placedDate.ifBlank { formatTimestamp(LocalDateTime.now()) }) else entry
            }
        }
        val customerName = string("customer_name", "buyer_name", "user_name")
            ?: supportObject?.string("customer_name", "buyer_name", "user_name")
            ?: customerObject?.string("name", "full_name")
            ?: "Buyer not available"
        val customerEmail = string("customer_email", "buyer_email")
            ?: supportObject?.string("customer_email", "buyer_email")
            ?: customerObject?.string("email")
            .orEmpty()
        val customerPhone = string("customer_phone", "buyer_phone", "phone")
            ?: supportObject?.string("customer_phone", "buyer_phone", "phone")
            ?: customerObject?.string("phone", "phone_number")
            .orEmpty()

        val resolvedOrderId = dto.id.asStringOrNull().orEmpty()
            .ifBlank { string("id", "order_id").orEmpty() }
            .ifBlank { summaryObject?.string("id", "order_id").orEmpty() }
            .ifBlank { supportObject?.string("id", "order_id").orEmpty() }
            .ifBlank { "order_$index" }
        return Order(
            id = resolvedOrderId,
            reference = dto.reference.orEmpty().ifBlank {
                string("reference", "reference_number", "order_number")
                    ?: summaryObject?.string("reference", "reference_number", "order_number")
                    ?: supportObject?.string("reference", "reference_number", "order_number")
                    ?: "FLORA Order"
            },
            customerId = string("customer_id", "buyer_id", "user_id")
                ?: supportObject?.string("customer_id", "buyer_id", "user_id")
                ?: customerObject?.string("id", "_id")
                .orEmpty(),
            customerName = customerName,
            customerEmail = customerEmail,
            customerPhone = customerPhone,
            customerLocation = string("customer_location")
                ?: supportObject?.string("customer_location")
                ?: listOfNotNull(shippingAddress?.municipality, shippingAddress?.country)
                    .filter { it.isNotBlank() }
                    .joinToString(", "),
            items = items,
            status = status,
            total = total,
            shippingCost = shippingCost,
            tax = tax,
            subtotal = subtotal,
            shippingMethod = string("shipping_method", "delivery_method")
                ?: supportObject?.string("shipping_method", "delivery_method")
                ?: summaryObject?.string("shipping_method", "delivery_method")
                .orEmpty(),
            paymentMethod = string("payment_method")
                ?: supportObject?.string("payment_method")
                ?: summaryObject?.string("payment_method")
                .orEmpty(),
            shippingAddress = shippingAddress,
            placedDate = placedDate,
            estimatedDelivery = formatOrderTimestamp(
                string("estimated_delivery", "estimated_delivery_date", "delivery_eta")
                    ?: supportObject?.string("estimated_delivery", "estimated_delivery_date", "delivery_eta")
                    ?: summaryObject?.string("estimated_delivery", "estimated_delivery_date", "delivery_eta")
                    .orEmpty(),
            ),
            trackingNumber = dto.trackingNumber.orEmpty().ifBlank {
                string("tracking_number", "tracking_no", "awb_number")
                    ?: supportObject?.string("tracking_number", "tracking_no", "awb_number")
                    ?: summaryObject?.string("tracking_number", "tracking_no", "awb_number")
                    ?: ""
            },
            carrier = dto.carrier.orEmpty().ifBlank {
                string("carrier", "shipping_carrier", "courier")
                    ?: supportObject?.string("carrier", "shipping_carrier", "courier")
                    ?: summaryObject?.string("carrier", "shipping_carrier", "courier")
                    ?: ""
            },
            shipmentStatus = dto.shipmentStatus.orEmpty().ifBlank {
                string("shipment_status", "shipping_status", "delivery_status", "tracking_status", "fulfillment_status")
                    ?: supportObject?.string("shipment_status", "shipping_status", "delivery_status", "tracking_status", "fulfillment_status")
                    ?: summaryObject?.string("shipment_status", "shipping_status", "delivery_status", "tracking_status", "fulfillment_status")
                    ?: status.name.lowercase()
            },
            imageUrl = items.firstOrNull()?.product?.imageUrl.orEmpty(),
            statusHistory = normalizedHistory.appendStatusEntry(
                status = status,
                timestamp = normalizedHistory.lastOrNull()?.timestamp.orEmpty().ifBlank { placedDate.ifBlank { formatTimestamp(LocalDateTime.now()) } },
                note = normalizedHistory.lastOrNull()?.note.orEmpty().ifBlank { statusNote(status) },
            ),
        )
    }

    private fun parseOrderItems(
        orderObject: JsonObject,
        sellerId: String,
        supportObject: JsonObject? = null,
    ): List<OrderItem> = (
        orderObject.arrayAt("items", "order_items", "products", "orderItems")
            ?: supportObject?.arrayAt("items", "order_items", "products", "orderItems")
            ?: orderObject.objectAt("order", "seller_order", "order_details", "details")?.arrayAt("items", "order_items", "products", "orderItems")
            ?: supportObject?.objectAt("order", "seller_order", "order_details", "details")?.arrayAt("items", "order_items", "products", "orderItems")
        )
        ?.mapIndexedNotNull { index, itemElement ->
            val itemObject = itemElement.asObjectOrNull() ?: return@mapIndexedNotNull null
            val productElement = itemObject.element("product", "product_details", "item") ?: itemElement
            val product = productElement.toSellerProduct(sellerId, index) ?: return@mapIndexedNotNull null
            OrderItem(
                id = itemObject.string("id", "order_item_id").orEmpty().ifBlank { "${product.id}_$index" },
                product = product,
                quantity = itemObject.int("quantity", "qty") ?: 1,
                variant = itemObject.string("variant", "selected_variant", "size").orEmpty(),
                unitPrice = itemObject.double("unit_price", "price") ?: product.price,
                lineTotal = itemObject.double("subtotal", "line_total", "total") ?: ((itemObject.int("quantity", "qty")
                    ?: 1) * (itemObject.double("unit_price", "price") ?: product.price)),
            )
        }
        .orEmpty()

    private fun JsonElement.toSellerProduct(
        defaultSellerId: String,
        index: Int = 0,
    ): Product? {
        val dto = gson.fromJson(this, ProductDto::class.java)
        val obj = asObjectOrNull() ?: return null
        val categoryObject = dto.category.asObjectOrNull()
        val storeObject = dto.store.asObjectOrNull() ?: dto.seller.asObjectOrNull()
        val resolvedSellerId = dto.storeId.asStringOrNull().orEmpty()
            .ifBlank { storeObject?.string("id", "_id", "seller_id", "store_id").orEmpty() }
            .ifBlank { defaultSellerId }
        val resolvedId = dto.id.asStringOrNull().orEmpty()
            .ifBlank { obj.string("id", "_id", "product_id").orEmpty() }
            .ifBlank { "order_product_${defaultSellerId.ifBlank { "unknown" }}_$index" }
        val resolvedImageUrl = resolveProductImageUrl(dto, obj)
        Log.d(
            TAG,
            "Order product mapped. id=$resolvedId title=${dto.name.orEmpty()} rawImage=${dto.image} imageUrl=${dto.imageUrl} finalImage=$resolvedImageUrl",
        )
        return Product(
            id = resolvedId,
            name = dto.name.orEmpty().ifBlank { obj.string("name", "title") ?: "FLORA Piece" },
            price = dto.price.asDoubleOrNull()
                ?: obj.double("price", "amount", "sale_price")
                ?: 0.0,
            imageUrl = resolvedImageUrl,
            studio = storeObject?.string("name", "store_name", "shop_name", "seller_name")
                ?: AppContainer.uiPreferencesRepository.getStoreConfiguration(resolvedSellerId).shopName.ifBlank { "FLORA Ceramics" },
            storeId = resolvedSellerId,
            category = categoryObject?.string("name", "title")
                ?: dto.category.asStringOrNull().orEmpty(),
            description = dto.description.orEmpty(),
            stockCount = dto.stock.asIntOrNull() ?: obj.int("stock", "stock_count", "quantity") ?: 0,
            isFavorited = dto.isFavorite == true,
            collectionLabel = (categoryObject?.string("name", "title")
                ?: dto.category.asStringOrNull().orEmpty()).uppercase(),
            story = dto.description.orEmpty(),
            images = buildList {
                add(resolvedImageUrl)
                dto.images.asArrayOrNull()?.forEach { image -> add(normalizeImageUrl(image.asStringOrNull().orEmpty())) }
            }.filter { it.isNotBlank() }.distinct(),
        )
    }

    private fun JsonObject.toAddress(): Address = Address(
        id = string("id", "address_id").orEmpty(),
        label = string("label").orEmpty(),
        fullName = string("full_name", "name").orEmpty(),
        phoneNumber = string("phone", "phone_number").orEmpty(),
        state = string("state").orEmpty(),
        municipality = string("municipality", "city").orEmpty(),
        neighborhood = string("neighborhood").orEmpty(),
        street = string("street_address", "street", "address_line_1").orEmpty(),
        postalCode = string("postal_code", "zip").orEmpty(),
        country = string("country").orEmpty(),
        isPrimary = element("is_primary", "default").asStringOrNull()?.toBoolean() == true,
    )

    private fun parseStatusHistory(
        orderObject: JsonObject,
        currentStatus: OrderStatus,
        placedDate: String,
        supportObject: JsonObject? = null,
    ): List<OrderStatusEntry> {
        val history = (orderObject.arrayAt("status_history", "history", "tracking_history", "timeline", "status_logs")
            ?: supportObject?.arrayAt("status_history", "history", "tracking_history", "timeline", "status_logs")
            ?: orderObject.objectAt("order", "seller_order", "order_details", "details")?.arrayAt("status_history", "history", "tracking_history", "timeline", "status_logs")
            ?: supportObject?.objectAt("order", "seller_order", "order_details", "details")?.arrayAt("status_history", "history", "tracking_history", "timeline", "status_logs"))
            ?.mapNotNull { entry ->
                val entryObject = entry.asObjectOrNull() ?: return@mapNotNull null
                val status = parseStatus(
                    entryObject.string("status", "order_status", "shipping_status", "delivery_status", "tracking_status", "fulfillment_status").orEmpty(),
                )
                OrderStatusEntry(
                    status = status,
                    timestamp = formatOrderTimestamp(entryObject.string("timestamp", "created_at", "date").orEmpty()),
                    note = entryObject.string("note", "message", "description").orEmpty(),
                )
            }
            .orEmpty()
        return if (history.isNotEmpty()) history else listOf(
            OrderStatusEntry(
                status = currentStatus,
                timestamp = placedDate.ifBlank { formatTimestamp(LocalDateTime.now()) },
                note = statusNote(currentStatus),
            ),
        )
    }

    private fun parseStatus(rawStatus: String): OrderStatus = when (rawStatus.trim().uppercase()) {
        "PENDING" -> OrderStatus.PENDING
        "CONFIRMED", "PAID", "PROCESSING" -> OrderStatus.CONFIRMED
        "HAND_CRAFTED", "HAND-CRAFTED", "CRAFTING", "PREPARING" -> OrderStatus.HAND_CRAFTED
        "SHIPPED", "IN_TRANSIT", "OUT_FOR_DELIVERY" -> OrderStatus.SHIPPED
        "DELIVERED", "COMPLETED" -> OrderStatus.DELIVERED
        "CANCELLED", "CANCELED", "REJECTED" -> OrderStatus.CANCELLED
        else -> OrderStatus.PENDING
    }

    private fun Order.containsSeller(sellerId: String): Boolean =
        items.any { item -> normalizedSellerId(item.product.storeId.ifBlank { inferredSellerId(item) }) == sellerId }

    private fun Order.forSeller(sellerId: String): Order {
        val sellerItems = items.filter { item ->
            normalizedSellerId(item.product.storeId.ifBlank { inferredSellerId(item) }) == sellerId
        }
        if (sellerItems.isEmpty() && items.isNotEmpty()) {
            return copy(
                imageUrl = items.firstOrNull()?.product?.imageUrl.orEmpty(),
            )
        }
        val sellerSubtotal = sellerItems.sumOf { it.product.price * it.quantity }
        val ratio = if (subtotal > 0.0) sellerSubtotal / subtotal else 1.0
        return copy(
            items = sellerItems,
            subtotal = sellerSubtotal,
            tax = tax * ratio,
            shippingCost = shippingCost * ratio,
            total = sellerSubtotal + (tax * ratio) + (shippingCost * ratio),
            trackingNumber = trackingNumber,
            carrier = carrier,
            shipmentStatus = shipmentStatus,
            imageUrl = sellerItems.firstOrNull()?.product?.imageUrl.orEmpty(),
        )
    }

    private fun formatTimestamp(dateTime: LocalDateTime): String = dateTime.format(timestampFormatter)

    private fun defaultCheckoutDraft(userId: String): CheckoutDraft {
        val address = accountSettingsRepository.getSavedAddresses(userId)
            .firstOrNull { it.isDefault }
            ?.toAddress()
        val paymentMethod = accountSettingsRepository.getPaymentMethods(userId)
            .firstOrNull()
            ?.let { _ -> "card" }
            ?: "card"
        return CheckoutDraft(
            address = address,
            shippingMethod = "home_delivery",
            paymentMethod = paymentMethod,
        )
    }

    private fun SavedAddressEntry.toAddress(): Address = Address(
        id = id,
        label = label,
        fullName = fullName,
        phoneNumber = phoneNumber,
        state = state,
        municipality = city,
        neighborhood = label,
        street = streetAddress,
        postalCode = postalCode,
        country = state,
        isPrimary = isDefault,
    )

    private fun normalizeImageUrl(raw: String): String = BackendUrlResolver.normalizeImageUrl(raw)

    private fun resolveProductImageUrl(
        productDto: ProductDto,
        obj: JsonObject,
    ): String = normalizeImageUrl(
        productDto.imageUrl
            ?: productDto.image
            ?: obj.string("image_url", "image", "image_path", "product_image", "thumbnail", "thumbnail_url", "photo")
            ?: productDto.images.asArrayOrNull()?.firstOrNull()?.asStringOrNull()
            ?: "",
    )

    private fun normalizedSellerId(sellerId: String): String = AppContainer.uiPreferencesRepository.normalizeSellerStoreId(
        when (sellerId) {
            "bachir@flora.com" -> "s1"
            else -> sellerId
        },
    )

    private fun inferredSellerId(item: OrderItem): String {
        val studio = item.product.studio.uppercase()
        return when {
            studio.contains("FLORA") || studio.contains("BACHIR") -> "s1"
            studio.contains("AURUM") -> "s2"
            studio.contains("ALBA") -> "s3"
            studio.contains("ELF-READ") || studio.contains("LOOM") -> "s4"
            studio.contains("OAK & BRASS") -> "s5"
            else -> ""
        }
    }

    private fun statusNote(status: OrderStatus): String = when (status) {
        OrderStatus.PENDING -> "Awaiting seller confirmation."
        OrderStatus.CONFIRMED -> "Seller confirmed the order."
        OrderStatus.HAND_CRAFTED -> "Order is being prepared."
        OrderStatus.SHIPPED -> "Order is on the way."
        OrderStatus.DELIVERED -> "Order was marked as delivered."
        OrderStatus.CANCELLED -> "Order was cancelled."
    }

    private fun updatedEstimatedDelivery(currentValue: String, status: OrderStatus): String = when (status) {
        OrderStatus.SHIPPED -> currentValue.ifBlank { "Out for delivery soon" }
        OrderStatus.DELIVERED -> "Delivered"
        else -> currentValue
    }

    private fun Order.withStatusUpdate(status: OrderStatus): Order = copy(
        status = status,
        estimatedDelivery = updatedEstimatedDelivery(estimatedDelivery, status),
        statusHistory = statusHistory.appendStatusEntry(
            status = status,
            timestamp = formatTimestamp(LocalDateTime.now()),
            note = statusNote(status),
        ),
    )

    private fun Order.mergeStatusFrom(source: Order): Order = copy(
        status = source.status,
        estimatedDelivery = source.estimatedDelivery.ifBlank { updatedEstimatedDelivery(estimatedDelivery, source.status) },
        statusHistory = statusHistory
            .appendStatusEntries(source.statusHistory)
            .appendStatusEntry(
                status = source.status,
                timestamp = source.statusHistory.lastOrNull()?.timestamp.orEmpty().ifBlank { formatTimestamp(LocalDateTime.now()) },
                note = source.statusHistory.lastOrNull()?.note.orEmpty().ifBlank { statusNote(source.status) },
            ),
    )

    private fun List<OrderStatusEntry>.appendStatusEntries(entries: List<OrderStatusEntry>): List<OrderStatusEntry> {
        var result = this
        entries.forEach { entry ->
            result = result.appendStatusEntry(entry.status, entry.timestamp, entry.note)
        }
        return result
    }

    private fun List<OrderStatusEntry>.appendStatusEntry(
        status: OrderStatus,
        timestamp: String,
        note: String,
    ): List<OrderStatusEntry> {
        val sanitizedTimestamp = timestamp.ifBlank { formatTimestamp(LocalDateTime.now()) }
        val existingIndex = indexOfFirst { it.status == status }
        val updatedEntry = OrderStatusEntry(
            status = status,
            timestamp = sanitizedTimestamp,
            note = note,
        )
        val withoutExisting = if (existingIndex >= 0) {
            toMutableList().apply { removeAt(existingIndex) }
        } else {
            toMutableList()
        }
        withoutExisting.add(updatedEntry)
        return withoutExisting
    }

    fun formatMoney(amount: Double): String = currencyFormatter.format(amount)

    fun formatOrderTimestamp(raw: String): String {
        val value = raw.trim()
        if (value.isBlank()) return value
        return runCatching {
            when {
                value.contains("T") -> Instant.parse(value)
                    .atZone(ZoneId.systemDefault())
                    .format(displayTimestampFormatter)
                else -> LocalDateTime.parse(value).format(displayTimestampFormatter)
            }
        }.getOrDefault(value)
    }

    private fun toBackendSellerStatus(status: OrderStatus): String = when (status) {
        OrderStatus.PENDING -> "pending"
        OrderStatus.CONFIRMED, OrderStatus.HAND_CRAFTED -> "processing"
        OrderStatus.SHIPPED -> "shipped"
        OrderStatus.DELIVERED -> "delivered"
        OrderStatus.CANCELLED -> "cancelled"
    }

}
