package com.example.myappmobile.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ShopFilterSelection(
    val categoryId: String = "",
    val subcategoryId: String = "",
    val type: String = "All",
    val minPrice: String = "",
    val maxPrice: String = "",
) {
    val hasActiveFilters: Boolean
        get() = categoryId.isNotBlank() || subcategoryId.isNotBlank() || type != "All" || minPrice.isNotBlank() || maxPrice.isNotBlank()
}

class ShopFilterRepository {
    private val _filters = MutableStateFlow(ShopFilterSelection())
    val filters: StateFlow<ShopFilterSelection> = _filters.asStateFlow()

    fun apply(selection: ShopFilterSelection) {
        _filters.value = selection
    }

    fun applyCategoryShortcut(categoryId: String, subcategoryId: String) {
        _filters.update {
            it.copy(
                categoryId = categoryId,
                subcategoryId = subcategoryId,
            )
        }
    }

    fun clear() {
        _filters.value = ShopFilterSelection()
    }
}
