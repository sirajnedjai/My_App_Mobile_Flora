package com.example.myappmobile.data.repository

import android.content.Context
import com.example.myappmobile.domain.Review
import com.example.myappmobile.domain.repository.AuthRepository
import com.example.myappmobile.domain.repository.OrderRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProductReviewRepository(
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
    private val reviewEligibilityService: ReviewEligibilityService,
) {
    private var appContext: Context? = null

    private val _reviewsByProduct = MutableStateFlow<Map<String, List<Review>>>(emptyMap())
    val reviewsByProduct: StateFlow<Map<String, List<Review>>> = _reviewsByProduct.asStateFlow()

    fun initialize(context: Context) {
        appContext = context.applicationContext
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _reviewsByProduct.value = prefs.all
            .filterKeys { it.startsWith(KEY_PREFIX) }
            .mapKeys { it.key.removePrefix(KEY_PREFIX) }
            .mapValues { (_, value) -> decodeReviews(value as? String) }
    }

    fun getReviews(productId: String): List<Review> = _reviewsByProduct.value[productId].orEmpty()

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

    fun addReview(productId: String, review: Review) {
        val updated = (_reviewsByProduct.value[productId].orEmpty() + review)
            .sortedByDescending { it.id }
        persist(productId, updated)
        _reviewsByProduct.update { it + (productId to updated) }
    }

    fun submitReview(
        productId: String,
        sellerId: String,
        rating: Int,
        text: String,
    ): Result<Review> {
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

        val currentUser = authRepository.currentUser.value
        val review = Review(
            id = System.currentTimeMillis().toString(),
            authorName = currentUser.fullName.ifBlank { "FLORA Collector" },
            rating = rating,
            text = trimmedText,
            isVerified = true,
            date = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
        )
        addReview(productId, review)
        return Result.success(review)
    }

    private fun persist(productId: String, reviews: List<Review>) {
        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("$KEY_PREFIX$productId", encodeReviews(reviews))
            .apply()
    }

    private fun encodeReviews(reviews: List<Review>): String = reviews.joinToString(RECORD_SEPARATOR) { review ->
        listOf(
            review.id,
            review.authorName,
            review.rating.toString(),
            review.text,
            review.isVerified.toString(),
            review.date,
        ).joinToString(FIELD_SEPARATOR) { field ->
            field
                .replace("\\", "\\\\")
                .replace(FIELD_SEPARATOR, "\\u001F")
                .replace(RECORD_SEPARATOR, "\\u001E")
        }
    }

    private fun decodeReviews(raw: String?): List<Review> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(RECORD_SEPARATOR).mapNotNull { record ->
            val fields = record.split(FIELD_SEPARATOR)
            if (fields.size < 6) return@mapNotNull null
            Review(
                id = fields[0].restoreEscapes(),
                authorName = fields[1].restoreEscapes(),
                rating = fields[2].toIntOrNull() ?: return@mapNotNull null,
                text = fields[3].restoreEscapes(),
                isVerified = fields[4].toBooleanStrictOrNull() ?: false,
                date = fields[5].restoreEscapes(),
            )
        }
    }

    private fun String.restoreEscapes(): String = this
        .replace("\\u001F", FIELD_SEPARATOR)
        .replace("\\u001E", RECORD_SEPARATOR)
        .replace("\\\\", "\\")

    private fun requireContext(): Context = checkNotNull(appContext) {
        "ProductReviewRepository is not initialized. Call initialize(context) first."
    }

    private companion object {
        const val PREFS_NAME = "flora_product_reviews"
        const val KEY_PREFIX = "product_reviews_"
        const val FIELD_SEPARATOR = "\u001F"
        const val RECORD_SEPARATOR = "\u001E"
    }
}
