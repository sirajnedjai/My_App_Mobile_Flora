package com.example.myappmobile.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SearchViewModel : ViewModel() {
    private val searchProductsUseCase = AppContainer.searchProductsUseCase

    private val _uiState = MutableStateFlow(
        SearchUiState()
    )
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        onQueryChange("")
    }

    fun onQueryChange(query: String) {
        val normalizedQuery = query.trimStart()
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    query = normalizedQuery,
                    results = searchProductsUseCase(normalizedQuery),
                )
            }
        }
    }
}
