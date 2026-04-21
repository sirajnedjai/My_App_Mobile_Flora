package com.example.myappmobile.data.repository

import android.content.Context
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.ReviewApiService
import com.example.myappmobile.data.remote.requireBody
import com.example.myappmobile.data.remote.toApiException
import com.example.myappmobile.domain.repository.AuthRepository
import com.example.myappmobile.domain.repository.OrderRepository
import com.google.gson.Gson

class ProductReviewRepository(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
    private val reviewEligibilityService: ReviewEligibilityService,
    private val reviewApiService: ReviewApiService,
    private val gson: Gson,
) {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun canUserReviewProduct(userId: String, productId: String): Boolean =
        reviewEligibilityService.canUserReviewProduct(
            userId = userId,
            productId = productId,
            orders = orderRepository.getOrdersForCustomer(userId),
        )

    fun getReviewEligibility(productId: String, sellerId: String): ReviewEligibility {
        val currentUser = authRepository.currentUser.value
        return reviewEligibilityService.evaluate(
            user = currentUser,
            productId = productId,
            sellerId = sellerId,
            orders = orderRepository.getOrdersForCustomer(currentUser.id),
        )
    }

    suspend fun submitReview(
        productId: String,
        orderId: String,
        sellerId: String,
        rating: Int,
        text: String,
    ): Result<Unit> {
        val eligibility = getReviewEligibility(productId = productId, sellerId = sellerId)
        if (!eligibility.canReview) {
            return Result.failure(
                IllegalAccessException(eligibility.message ?: ReviewEligibilityService.REVIEW_RESTRICTION_MESSAGE),
            )
        }
        if (rating !in 1..5) {
            return Result.failure(IllegalArgumentException("Please choose a valid star rating before submitting."))
        }

        val trimmedText = text.trim()
        if (trimmedText.isBlank()) {
            return Result.failure(IllegalArgumentException("Please write a short review before submitting."))
        }
        if (orderId.isBlank()) {
            return Result.failure(
                IllegalArgumentException("You can only review products you purchased and received."),
            )
        }

        return runCatching {
            reviewApiService.createReview(
                mapOf(
                    "product_id" to productId,
                    "order_id" to orderId,
                    "seller_id" to sellerId.ifBlank { null },
                    "rating" to rating,
                    "comment" to trimmedText,
                ),
            ).requireBody(gson)
            val reviewerName = authRepository.currentUser.value.fullName
            val productName = orderRepository.getOrdersForCustomer(authRepository.currentUser.value.id)
                .flatMap { it.items }
                .firstOrNull { it.product.id == productId }
                ?.product
                ?.name
                .orEmpty()
            AppContainer.notificationService.sendSellerReviewNotification(
                sellerId = sellerId,
                productId = productId,
                productName = productName,
                reviewerName = reviewerName,
                reviewSnippet = trimmedText.take(120),
            )
            Unit
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it.toApiException()) },
        )
    }
}
