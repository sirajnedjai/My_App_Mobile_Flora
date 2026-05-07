package com.example.myappmobile.presentation.seller.about

import com.example.myappmobile.domain.model.Store

data class AboutStoreUiState(
    val store: Store? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
