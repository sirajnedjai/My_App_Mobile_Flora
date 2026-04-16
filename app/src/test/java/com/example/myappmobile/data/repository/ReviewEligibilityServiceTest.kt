package com.example.myappmobile.data.repository

import com.example.myappmobile.domain.model.Order
import com.example.myappmobile.domain.model.OrderItem
import com.example.myappmobile.domain.model.OrderStatus
import com.example.myappmobile.domain.model.Product
import com.example.myappmobile.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewEligibilityServiceTest {

    private val service = ReviewEligibilityService()

    @Test
    fun `eligible buyer can review only after delivered order for exact product`() {
        val user = User(id = "u1", fullName = "Buyer", email = "buyer@flora.com", isAuthenticated = true)
        val deliveredOrder = order(
            customerId = user.id,
            status = OrderStatus.DELIVERED,
            productIds = listOf("product_1"),
        )

        val eligibility = service.evaluate(
            user = user,
            productId = "product_1",
            sellerId = "seller_7",
            orders = listOf(deliveredOrder),
        )

        assertTrue(eligibility.canReview)
        assertEquals(null, eligibility.message)
    }

    @Test
    fun `buyer cannot review when matching order is not delivered`() {
        val user = User(id = "u1", fullName = "Buyer", email = "buyer@flora.com", isAuthenticated = true)
        val shippedOrder = order(
            customerId = user.id,
            status = OrderStatus.SHIPPED,
            productIds = listOf("product_1"),
        )

        val eligibility = service.evaluate(
            user = user,
            productId = "product_1",
            sellerId = "seller_7",
            orders = listOf(shippedOrder),
        )

        assertFalse(eligibility.canReview)
        assertEquals(ReviewEligibilityService.REVIEW_RESTRICTION_MESSAGE, eligibility.message)
    }

    @Test
    fun `seller account cannot review own product even if delivered`() {
        val seller = User(
            id = "seller_7",
            fullName = "Seller",
            email = "seller@flora.com",
            isAuthenticated = true,
            isSeller = true,
        )
        val deliveredOrder = order(
            customerId = seller.id,
            status = OrderStatus.DELIVERED,
            productIds = listOf("product_1"),
        )

        val eligibility = service.evaluate(
            user = seller,
            productId = "product_1",
            sellerId = seller.id,
            orders = listOf(deliveredOrder),
        )

        assertFalse(eligibility.canReview)
        assertEquals(ReviewEligibilityService.SELLER_RESTRICTION_MESSAGE, eligibility.message)
    }

    private fun order(
        customerId: String,
        status: OrderStatus,
        productIds: List<String>,
    ): Order = Order(
        id = "order_${customerId}_$status",
        reference = "REF-1",
        customerId = customerId,
        items = productIds.mapIndexed { index, productId ->
            OrderItem(
                id = "item_$index",
                product = Product(
                    id = productId,
                    name = "FLORA Piece",
                    price = 12.0,
                    imageUrl = "",
                    studio = "FLORA",
                ),
            )
        },
        status = status,
    )
}
