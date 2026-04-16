package com.example.myappmobile.presentation.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.access.RoleAccessManager
import com.example.myappmobile.core.catalog.FloraCatalog
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.MockData
import com.example.myappmobile.data.repository.ShopFilterSelection
import com.example.myappmobile.domain.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShopViewModel : ViewModel() {
    private companion object {
        const val PAGE_SIZE = 4
    }

    private val productRepository = AppContainer.productRepository
    private val authRepository = AppContainer.authRepository
    private val filterRepository = AppContainer.shopFilterRepository

    private val sortOptions = listOf(
        ShopSortUi.POPULAR,
        ShopSortUi.NEWEST,
        ShopSortUi.CURATED,
    )

    private val controlsState = MutableStateFlow(
        ShopUiState(
            sortOptions = sortOptions,
            banner = ShopBannerUi(
                title = "FLORA Spring Atelier",
                subtitle = "A curated selection of handmade pieces, quietly luxurious and made to live beautifully.",
                ctaText = "Explore Collection",
                imageUrl = MockData.banner.imageUrl,
            ),
            isLoading = true,
        )
    )
    val uiState: StateFlow<ShopUiState> = combine(
        productRepository.observeAllProducts(),
        controlsState,
        filterRepository.filters,
        authRepository.currentUser,
    ) { products, state, filters, user ->
        val access = RoleAccessManager.capabilities(user)
        val effectiveCategoryId = filters.categoryId.ifBlank { state.selectedCategoryId }
        val filteredProducts = sortProducts(
            products = products
                .filterByQuery(state.searchQuery)
                .filterByCategory(effectiveCategoryId)
                .filterByAdvancedFilters(filters),
            sortId = state.selectedSortId,
        )
        state.copy(
            categories = buildCategoryOptions(products),
            products = filteredProducts,
            visibleProducts = filteredProducts.take(state.visibleCount),
            canLoadMore = filteredProducts.size > state.visibleCount,
            appliedFilters = filters,
            activeFiltersSummary = filters.summary(),
            canUseWishlist = access.canUseWishlist,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = controlsState.value,
    )

    fun onSearchQueryChange(query: String) {
        controlsState.update { it.copy(searchQuery = query, visibleCount = PAGE_SIZE) }
    }

    fun onCategorySelected(categoryId: String) {
        controlsState.update { it.copy(selectedCategoryId = categoryId, visibleCount = PAGE_SIZE) }
    }

    fun onSortSelected(sortId: String) {
        controlsState.update { it.copy(selectedSortId = sortId, visibleCount = PAGE_SIZE) }
    }

    fun onLoadMore() {
        controlsState.update { state ->
            state.copy(visibleCount = state.visibleCount + PAGE_SIZE)
        }
    }

    fun onToggleFavorite(productId: String) {
        if (!uiState.value.canUseWishlist) return
        viewModelScope.launch {
            productRepository.toggleFavorite(productId)
        }
    }

    fun clearFilters() {
        filterRepository.clear()
    }

    private fun buildCategoryOptions(products: List<Product>): List<ShopCategoryUi> = listOf(ShopCategoryUi.ALL) +
        products
            .map { product -> ShopCategoryUi(product.category.id, product.category.name) }
            .distinctBy(ShopCategoryUi::id)
            .sortedBy(ShopCategoryUi::title)

    private fun List<Product>.filterByQuery(query: String): List<Product> = if (query.isBlank()) {
        this
    } else {
        filter { product ->
            product.name.contains(query, ignoreCase = true) ||
                product.studio.contains(query, ignoreCase = true) ||
                product.category.name.contains(query, ignoreCase = true)
        }
    }

    private fun List<Product>.filterByCategory(categoryId: String): List<Product> =
        if (categoryId == ShopCategoryUi.ALL.id) this else filter { it.category.id == categoryId }

    private fun List<Product>.filterByAdvancedFilters(filters: ShopFilterSelection): List<Product> = filter { product ->
        val min = filters.minPrice.toDoubleOrNull()
        val max = filters.maxPrice.toDoubleOrNull()
        FloraCatalog.matchesCategory(product, filters.categoryId) &&
            FloraCatalog.matchesSubcategory(product, filters.categoryId, filters.subcategoryId) &&
            FloraCatalog.matchesType(product, filters.type) &&
            (min == null || product.price >= min) &&
            (max == null || product.price <= max)
    }

    private fun sortProducts(products: List<Product>, sortId: String): List<Product> = when (sortId) {
        ShopSortUi.NEWEST.id -> products.sortedByDescending(Product::id)
        ShopSortUi.CURATED.id -> products.sortedByDescending { it.isFavorited }.sortedBy(Product::studio)
        else -> products.sortedByDescending(Product::price)
    }

    private fun ShopFilterSelection.summary(): List<String> = buildList {
        if (categoryId.isNotBlank()) add(FloraCatalog.categoryLabel(categoryId))
        if (subcategoryId.isNotBlank()) add(FloraCatalog.subcategoryLabel(categoryId, subcategoryId))
        if (type != "All") add(type)
        if (minPrice.isNotBlank() || maxPrice.isNotBlank()) add("$${minPrice.ifBlank { "0" }} - $${maxPrice.ifBlank { "∞" }}")
    }.filter { it.isNotBlank() }
}
