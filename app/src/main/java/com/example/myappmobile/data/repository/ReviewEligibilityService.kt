package com.example.myappmobile.data.repository

import com.example.myappmobile.domain.model.Order
import com.example.myappmobile.domain.model.OrderStatus
import com.example.myappmobile.domain.model.User

data class ReviewEligibility(
    val canReview: Boolean,
    val message: String? = null,
    val orderId: String? = null,
)

class ReviewEligibilityService {

    fun canUserReviewProduct(
        userId: String,
        productId: String,
        orders: List<Order>,
    ): Boolean = orders.any { order ->
        order.customerId == userId &&
            order.status == OrderStatus.DELIVERED &&
            order.items.any { item -> item.product.id == productId }
    }

    fun evaluate(
        user: User,
        productId: String,
        sellerId: String,
        orders: List<Order>,
    ): ReviewEligibility {
        if (!user.isAuthenticated) {
            return ReviewEligibility(
                canReview = false,
                message = REVIEW_RESTRICTION_MESSAGE,
            )
        }

        if (user.isSeller || sellerId == user.id) {
            return ReviewEligibility(
                canReview = false,
                message = SELLER_RESTRICTION_MESSAGE,
            )
        }

        val eligibleOrderId = eligibleOrderId(
            userId = user.id,
            productId = productId,
            orders = orders,
        )

        return if (eligibleOrderId != null) {
            ReviewEligibility(
                canReview = true,
                orderId = eligibleOrderId,
            )
        } else {
            ReviewEligibility(
                canReview = false,
                message = REVIEW_RESTRICTION_MESSAGE,
            )
        }
    }

    fun eligibleOrderId(
        userId: String,
        productId: String,
        orders: List<Order>,
    ): String? = orders
        .asReversed()
        .firstOrNull { order ->
            order.customerId == userId &&
                order.status == OrderStatus.DELIVERED &&
                order.items.any { item -> item.product.id == productId }
        }
        ?.id

    companion object {
        const val REVIEW_RESTRICTION_MESSAGE =
            "Please order this product first and wait until it is marked as delivered before leaving a review."
        const val SELLER_RESTRICTION_MESSAGE =
            "Seller accounts cannot leave customer reviews. Only verified buyers can rate delivered products."
    }
}
