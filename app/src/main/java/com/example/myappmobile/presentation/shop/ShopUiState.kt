package com.example.myappmobile.presentation.shop

import com.example.myappmobile.domain.Product

data class ShopUiState(
    val title: String = "Discover",
    val searchQuery: String = "",
    val selectedCategoryId: String = ShopCategoryUi.ALL.id,
    val selectedSortId: String = ShopSortUi.POPULAR.id,
    val categories: List<ShopCategoryUi> = emptyList(),
    val sortOptions: List<ShopSortUi> = emptyList(),
    val banner: ShopBannerUi? = null,
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true,
)

data class ShopCategoryUi(
    val id: String,
    val title: String,
) {
    companion object {
        val ALL = ShopCategoryUi("all", "Handmade")
    }
}

data class ShopSortUi(
    val id: String,
    val title: String,
) {
    companion object {
        val POPULAR = ShopSortUi("popular", "Popular")
        val NEWEST = ShopSortUi("newest", "Newest")
        val CURATED = ShopSortUi("curated", "Curated")
    }
}

data class ShopBannerUi(
    val title: String,
    val subtitle: String,
    val ctaText: String,
    val imageUrl: String,
)
