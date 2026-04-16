package com.example.myappmobile.core.access

import com.example.myappmobile.domain.model.User

data class RoleCapabilities(
    val isBuyer: Boolean,
    val isSeller: Boolean,
    val canUseWishlist: Boolean,
    val canWriteReviews: Boolean,
    val buyersOnlyMessage: String = "This feature is available for buyers only.",
)

object RoleAccessManager {
    fun capabilities(user: User?): RoleCapabilities {
        val isSeller = user?.isSeller == true
        val isBuyer = user?.isAuthenticated == true && !isSeller
        return RoleCapabilities(
            isBuyer = isBuyer,
            isSeller = isSeller,
            canUseWishlist = isBuyer,
            canWriteReviews = isBuyer,
        )
    }
}
