package com.example.myappmobile.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val searchProductsUseCase = AppContainer.searchProductsUseCase
    private val searchHistoryRepository = AppContainer.searchHistoryRepository
    private var allProducts = emptyList<com.example.myappmobile.domain.Product>()

    private val _uiState = MutableStateFlow(
        SearchUiState()
    )
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            searchHistoryRepository.history.collect { history ->
                _uiState.update { it.copy(recentSearches = history) }
            }
        }

        viewModelScope.launch {
            allProducts = searchProductsUseCase("")
            _uiState.update {
                it.copy(
                    availableCategories = listOf("All") + allProducts.map { product -> product.category.name }.distinct().sorted(),
                )
            }
        }
    }

    fun onQueryChange(query: String) {
        val normalizedQuery = query.trimStart()
        _uiState.update { it.copy(query = normalizedQuery) }
    }

    fun onCategorySelected(category: String) {
        val shouldRefresh = _uiState.value.hasSearched
        _uiState.update { it.copy(selectedCategory = category, isLoading = shouldRefresh) }
        if (shouldRefresh) {
            viewModelScope.launch { refreshResults(addToRecent = false) }
        }
    }

    fun onSortSelected(sort: SearchSortUi) {
        val shouldRefresh = _uiState.value.hasSearched
        _uiState.update { it.copy(selectedSort = sort, isLoading = shouldRefresh) }
        if (shouldRefresh) {
            viewModelScope.launch { refreshResults(addToRecent = false) }
        }
    }

    fun onSuggestionSelected(keyword: String) {
        _uiState.update { it.copy(query = keyword, isLoading = true, hasSearched = true) }
        viewModelScope.launch { refreshResults(addToRecent = true) }
    }

    fun onSearchSubmitted() {
        val query = _uiState.value.query.trim()
        _uiState.update {
            it.copy(
                query = query,
                isLoading = true,
                hasSearched = true,
            )
        }
        viewModelScope.launch { refreshResults(addToRecent = true) }
    }

    fun removeRecentSearch(keyword: String) {
        searchHistoryRepository.removeSearch(keyword)
    }

    fun clearRecentSearches() {
        searchHistoryRepository.clearHistory()
    }

    private suspend fun refreshResults(addToRecent: Boolean = false) {
        val snapshot = _uiState.value
        val searched = searchProductsUseCase(snapshot.query)
        val filtered = searched
            .filterByCategory(snapshot.selectedCategory)
            .sortBy(snapshot.selectedSort, snapshot.query)

        _uiState.update {
            it.copy(
                isLoading = false,
                results = filtered,
            )
        }
        if (addToRecent && snapshot.query.isNotBlank()) {
            searchHistoryRepository.addSearch(snapshot.query)
        }
    }

    private fun List<com.example.myappmobile.domain.Product>.filterByCategory(category: String): List<com.example.myappmobile.domain.Product> =
        if (category == "All") this else filter { it.category.name == category }

    private fun List<com.example.myappmobile.domain.Product>.sortBy(
        sort: SearchSortUi,
        query: String,
    ): List<com.example.myappmobile.domain.Product> = when (sort) {
        SearchSortUi.RELEVANCE -> sortedWith(
            compareBy<com.example.myappmobile.domain.Product> { product ->
                when {
                    query.isBlank() -> 0
                    product.name.contains(query, ignoreCase = true) -> 0
                    product.studio.contains(query, ignoreCase = true) -> 1
                    else -> 2
                }
            }.thenBy { it.name },
        )
        SearchSortUi.PRICE_LOW_TO_HIGH -> sortedBy { it.price }
        SearchSortUi.PRICE_HIGH_TO_LOW -> sortedByDescending { it.price }
    }
}
