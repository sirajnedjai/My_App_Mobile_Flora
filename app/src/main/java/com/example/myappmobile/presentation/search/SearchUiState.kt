package com.example.myappmobile.presentation.search

import androidx.annotation.StringRes
import com.example.myappmobile.R

import com.example.myappmobile.domain.Product

enum class SearchSortUi(@StringRes val labelRes: Int) {
    RELEVANCE(R.string.search_sort_relevance),
    PRICE_LOW_TO_HIGH(R.string.search_sort_price_low),
    PRICE_HIGH_TO_LOW(R.string.search_sort_price_high),
}

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val results: List<Product> = emptyList(),
    val error: String? = null,
    val availableCategories: List<String> = emptyList(),
    val selectedCategory: String = "All",
    val selectedSort: SearchSortUi = SearchSortUi.RELEVANCE,
    val recentSearches: List<String> = emptyList(),
    val trendingKeywords: List<String> = listOf("Ceramics", "Handwoven", "Pearl", "Decor"),
)
