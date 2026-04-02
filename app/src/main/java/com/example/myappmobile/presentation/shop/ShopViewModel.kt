package com.example.myappmobile.presentation.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.MockData
import com.example.myappmobile.domain.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShopViewModel : ViewModel() {

    private val productRepository = AppContainer.productRepository

    private val categoryOptions = listOf(
        ShopCategoryUi.ALL,
        ShopCategoryUi("jewelry", "Accessories"),
        ShopCategoryUi("home", "Decor"),
        ShopCategoryUi("textiles", "Beauty"),
        ShopCategoryUi("ceramics", "Food"),
    )

    private val sortOptions = listOf(
        ShopSortUi.POPULAR,
        ShopSortUi.NEWEST,
        ShopSortUi.CURATED,
    )

    private val _uiState = MutableStateFlow(
        ShopUiState(
            categories = categoryOptions,
            sortOptions = sortOptions,
            banner = ShopBannerUi(
                title = "FLORA Spring Atelier",
                subtitle = "A curated selection of handmade pieces, quietly luxurious and made to live beautifully.",
                ctaText = "Explore Collection",
                imageUrl = MockData.banner.imageUrl,
            ),
        )
    )
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    init {
        refreshProducts()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        refreshProducts()
    }

    fun onCategorySelected(categoryId: String) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        refreshProducts()
    }

    fun onSortSelected(sortId: String) {
        _uiState.update { it.copy(selectedSortId = sortId) }
        refreshProducts()
    }

    fun onToggleFavorite(productId: String) {
        viewModelScope.launch {
            productRepository.toggleFavorite(productId)
            refreshProducts()
        }
    }

    private fun refreshProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            var products = productRepository.getAllProducts()
            val state = _uiState.value

            if (state.searchQuery.isNotBlank()) {
                products = products.filter { product ->
                    product.name.contains(state.searchQuery, ignoreCase = true) ||
                        product.studio.contains(state.searchQuery, ignoreCase = true) ||
                        product.category.name.contains(state.searchQuery, ignoreCase = true)
                }
            }

            if (state.selectedCategoryId != ShopCategoryUi.ALL.id) {
                products = products.filter { it.category.id == state.selectedCategoryId }
            }

            products = sortProducts(products, state.selectedSortId)

            _uiState.update {
                it.copy(
                    products = products,
                    isLoading = false,
                )
            }
        }
    }

    private fun sortProducts(products: List<Product>, sortId: String): List<Product> = when (sortId) {
        ShopSortUi.NEWEST.id -> products.sortedByDescending(Product::id)
        ShopSortUi.CURATED.id -> products.sortedByDescending { it.isFavorited }.sortedBy(Product::studio)
        else -> products.sortedByDescending(Product::price)
    }
}
