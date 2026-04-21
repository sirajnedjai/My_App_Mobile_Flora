package com.example.myappmobile.presentation.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.toApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private companion object {
        const val TAG = "SearchViewModel"
    }

    private val searchProductsUseCase = AppContainer.searchProductsUseCase
    private val productRepository = AppContainer.productRepository
    private val searchHistoryRepository = AppContainer.searchHistoryRepository

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
            val allProducts = runCatching { productRepository.getAllProducts() }
                .onFailure { error ->
                    Log.d(TAG, "Unable to preload categories for search. error=${error.toApiException().message}")
                }
                .getOrDefault(emptyList())
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
        _uiState.update { it.copy(selectedCategory = category, isLoading = shouldRefresh, error = null) }
        if (shouldRefresh) {
            viewModelScope.launch { refreshResults(addToRecent = false) }
        }
    }

    fun onSortSelected(sort: SearchSortUi) {
        val shouldRefresh = _uiState.value.hasSearched
        _uiState.update { it.copy(selectedSort = sort, isLoading = shouldRefresh, error = null) }
        if (shouldRefresh) {
            viewModelScope.launch { refreshResults(addToRecent = false) }
        }
    }

    fun onSuggestionSelected(keyword: String) {
        _uiState.update { it.copy(query = keyword, isLoading = true, hasSearched = true, error = null) }
        viewModelScope.launch { refreshResults(addToRecent = true) }
    }

    fun onSearchSubmitted() {
        val query = _uiState.value.query.trim()
        _uiState.update {
            it.copy(
                query = query,
                isLoading = true,
                hasSearched = true,
                error = null,
            )
        }
        viewModelScope.launch { refreshResults(addToRecent = true) }
    }

    fun retry() {
        if (_uiState.value.hasSearched) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            viewModelScope.launch { refreshResults(addToRecent = false) }
        }
    }

    fun removeRecentSearch(keyword: String) {
        searchHistoryRepository.removeSearch(keyword)
    }

    fun clearRecentSearches() {
        searchHistoryRepository.clearHistory()
    }

    private suspend fun refreshResults(addToRecent: Boolean = false) {
        val snapshot = _uiState.value
        val category = snapshot.selectedCategory.takeUnless { it == "All" }.orEmpty()
        Log.d(TAG, "Submitting search query='${snapshot.query}' category='$category' sort='${snapshot.selectedSort.name}'")

        runCatching {
            val searched = searchProductsUseCase(snapshot.query, category)
            val sorted = searched.sortBy(snapshot.selectedSort, snapshot.query)
            Log.d(TAG, "Search results loaded. count=${sorted.size}")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    results = sorted,
                    error = null,
                )
            }
            if (addToRecent && snapshot.query.isNotBlank()) {
                searchHistoryRepository.addSearch(snapshot.query)
            }
        }.onFailure { error ->
            val apiError = error.toApiException()
            Log.d(TAG, "Search API failed. query='${snapshot.query}' category='$category' error=${apiError.message}")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    results = emptyList(),
                    error = apiError.message,
                )
            }
        }
    }

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
