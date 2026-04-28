package com.example.myappmobile.presentation.home

import com.example.myappmobile.domain.BannerData
import com.example.myappmobile.domain.Category
import com.example.myappmobile.domain.Product
import com.example.myappmobile.domain.model.SellerApprovalStatus
import com.example.myappmobile.domain.model.User

data class HomeUiState(
    val isLoading: Boolean = true,
    val currentUser: User? = null,
    val accountStatus: SellerApprovalStatus = SellerApprovalStatus.NOT_VERIFIED,
    val banner: BannerData? = null,
    val categories: List<Category> = emptyList(),
    val featuredProducts: List<Product> = emptyList(),
    val newArrivals: List<Product> = emptyList(),
    val emailInput: String = "",
    val isSubscribed: Boolean = false,
    val canUseWishlist: Boolean = true,
    val error: String? = null,
    val favoriteMessage: String? = null,
    val pendingFavoriteIds: Set<String> = emptySet(),
) {
    val hasContent: Boolean
        get() = banner != null ||
            categories.isNotEmpty() ||
            featuredProducts.isNotEmpty() ||
            newArrivals.isNotEmpty()
}
