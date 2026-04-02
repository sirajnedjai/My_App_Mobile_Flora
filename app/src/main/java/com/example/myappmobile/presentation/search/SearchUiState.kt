package com.example.myappmobile.presentation.search

import com.example.myappmobile.domain.Product

data class SearchUiState(
    val query: String = "",
    val results: List<Product> = emptyList(),
)
