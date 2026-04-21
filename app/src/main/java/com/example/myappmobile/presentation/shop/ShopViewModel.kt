package com.example.myappmobile.presentation.shop

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.access.RoleAccessManager
import com.example.myappmobile.core.catalog.FloraCatalog
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.toApiException
import com.example.myappmobile.data.repository.ShopFilterSelection
import com.example.myappmobile.domain.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShopViewModel : ViewModel() {
    private companion object {
        const val PAGE_SIZE = 4
        const val TAG = "ShopViewModel"
    }

    private val productRepository = AppContainer.productRepository
    private val authRepository = AppContainer.authRepository
    private val filterRepository = AppContainer.shopFilterRepository
    private val remoteProducts = MutableStateFlow<List<Product>>(emptyList())

    private val sortOptions = listOf(
        ShopSortUi.POPULAR,
        ShopSortUi.NEWEST,
        ShopSortUi.CURATED,
    )
    private val favoriteUiState = combine(
        authRepository.currentUser,
        productRepository.favoriteMessage,
        productRepository.favoriteOperationProductIds,
    ) { user, favoriteMessage, pendingFavoriteIds ->
        FavoriteUiState(
            canUseWishlist = RoleAccessManager.capabilities(user).canUseWishlist,
            favoriteMessage = favoriteMessage,
            pendingFavoriteIds = pendingFavoriteIds,
        )
    }

    private val controlsState = MutableStateFlow(
        ShopUiState(
            sortOptions = sortOptions,
            isLoading = true,
        ),
    )

    init {
        loadCategories()
        viewModelScope.launch {
            filterRepository.filters.collectLatest {
                refreshProducts()
            }
        }
        refreshProducts()
    }

    val uiState: StateFlow<ShopUiState> = combine(
        remoteProducts,
        productRepository.observeAllProducts(),
        controlsState,
        filterRepository.filters,
        favoriteUiState,
    ) { products, allProducts, state, filters, favoriteUiState ->
        val effectiveCategoryId = filters.categoryId.ifBlank { state.selectedCategoryId }
        val productsById = allProducts.associateBy(Product::id)
        val synchronizedProducts = products.map { product ->
            val localProduct = productsById[product.id]
            product.copy(
                imageUrl = product.imageUrl.ifBlank { localProduct?.imageUrl.orEmpty() },
                studio = product.studio.ifBlank { localProduct?.studio.orEmpty() },
                isFavorited = localProduct?.isFavorited ?: product.isFavorited,
            )
        }
        val filteredProducts = sortProducts(
            products = synchronizedProducts
                .filterByAdvancedFilters(filters),
            selectedCategoryId = effectiveCategoryId,
            query = state.searchQuery,
            sortId = state.selectedSortId,
        )
        val banner = deriveBanner(filteredProducts.ifEmpty { synchronizedProducts })
        state.copy(
            banner = banner,
            products = filteredProducts,
            visibleProducts = filteredProducts.take(state.visibleCount),
            canLoadMore = filteredProducts.size > state.visibleCount,
            appliedFilters = filters,
            activeFiltersSummary = filters.summary(),
            canUseWishlist = favoriteUiState.canUseWishlist,
            isLoading = state.isLoading && synchronizedProducts.isEmpty(),
            favoriteMessage = favoriteUiState.favoriteMessage,
            pendingFavoriteIds = favoriteUiState.pendingFavoriteIds,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = controlsState.value,
    )

    fun retry() {
        refreshProducts()
    }

    fun onSearchQueryChange(query: String) {
        controlsState.update { it.copy(searchQuery = query, visibleCount = PAGE_SIZE) }
        refreshProducts()
    }

    fun onCategorySelected(categoryId: String) {
        controlsState.update { it.copy(selectedCategoryId = categoryId, visibleCount = PAGE_SIZE) }
        refreshProducts()
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
        if (!uiState.value.canUseWishlist || productId in uiState.value.pendingFavoriteIds) return
        viewModelScope.launch {
            productRepository.toggleFavorite(productId)
        }
    }

    fun clearFavoriteMessage() {
        productRepository.clearFavoriteMessage()
    }

    fun clearFilters() {
        filterRepository.clear()
        refreshProducts()
    }

    private fun refreshProducts() {
        viewModelScope.launch {
            val snapshot = controlsState.value
            val filters = filterRepository.filters.value
            val apiCategory = resolveApiCategory(snapshot, filters)
            Log.d(
                TAG,
                "Refreshing shop products. query='${snapshot.searchQuery}' apiCategory='$apiCategory' filters=$filters",
            )
            controlsState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val products = AppContainer.searchProductsUseCase(snapshot.searchQuery, apiCategory)
                Log.d(TAG, "Shop API success. products=${products.size}")
                Log.d(TAG, "Shop pagination metadata not provided by current endpoint.")
                remoteProducts.value = products
                controlsState.update { it.copy(isLoading = false, error = null) }
            }.onFailure { error ->
                val apiError = error.toApiException()
                Log.d(TAG, "Shop API failed: ${apiError.message}")
                remoteProducts.value = emptyList()
                controlsState.update {
                    it.copy(
                        isLoading = false,
                        error = apiError.message,
                    )
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val catalog = runCatching { productRepository.getAllProducts() }.getOrDefault(emptyList())
            controlsState.update { state ->
                state.copy(categories = buildCategoryOptions(catalog))
            }
        }
    }

    private fun deriveBanner(products: List<Product>): ShopBannerUi? {
        val leadProduct = products.firstOrNull() ?: return null
        return ShopBannerUi(
            title = leadProduct.name,
            subtitle = "Explore handcrafted ${leadProduct.category.name.lowercase()} from ${leadProduct.studio} and discover more FLORA pieces.",
            ctaText = "Explore Collection",
            imageUrl = leadProduct.imageUrl,
        )
    }

    private fun buildCategoryOptions(products: List<Product>): List<ShopCategoryUi> = listOf(ShopCategoryUi.ALL) +
        products
            .map { product -> ShopCategoryUi(product.category.id, product.category.name) }
            .filter { it.title.isNotBlank() }
            .distinctBy(ShopCategoryUi::id)
            .sortedBy(ShopCategoryUi::title)

    private fun List<Product>.filterByAdvancedFilters(filters: ShopFilterSelection): List<Product> = filter { product ->
        val min = filters.minPrice.toDoubleOrNull()
        val max = filters.maxPrice.toDoubleOrNull()
        FloraCatalog.matchesSubcategory(product, filters.categoryId, filters.subcategoryId) &&
            FloraCatalog.matchesType(product, filters.type) &&
            (min == null || product.price >= min) &&
            (max == null || product.price <= max)
    }

    private fun sortProducts(
        products: List<Product>,
        selectedCategoryId: String,
        query: String,
        sortId: String,
    ): List<Product> {
        val locallyRefined = products
            .let { results ->
                if (selectedCategoryId == ShopCategoryUi.ALL.id) results else results.filter { it.category.id == selectedCategoryId }
            }
            .let { results ->
                if (query.isBlank()) results else results.filter { product ->
                    product.name.contains(query, ignoreCase = true) ||
                        product.studio.contains(query, ignoreCase = true) ||
                        product.category.name.contains(query, ignoreCase = true)
                }
            }

        return when (sortId) {
            ShopSortUi.NEWEST.id -> locallyRefined.sortedByDescending(Product::id)
            ShopSortUi.CURATED.id -> locallyRefined.sortedByDescending { it.isFavorited }.sortedBy(Product::studio)
            else -> locallyRefined.sortedByDescending(Product::price)
        }
    }

    private fun resolveApiCategory(state: ShopUiState, filters: ShopFilterSelection): String {
        if (filters.categoryId.isNotBlank()) {
            return FloraCatalog.categoryLabel(filters.categoryId)
        }
        return state.categories.firstOrNull { it.id == state.selectedCategoryId }
            ?.title
            ?.takeUnless { it.equals(ShopCategoryUi.ALL.title, ignoreCase = true) }
            .orEmpty()
    }

    private fun ShopFilterSelection.summary(): List<String> = buildList {
        if (categoryId.isNotBlank()) add(FloraCatalog.categoryLabel(categoryId))
        if (subcategoryId.isNotBlank()) add(FloraCatalog.subcategoryLabel(categoryId, subcategoryId))
        if (type != "All") add(type)
        if (minPrice.isNotBlank() || maxPrice.isNotBlank()) add("$${minPrice.ifBlank { "0" }} - $${maxPrice.ifBlank { "∞" }}")
    }.filter { it.isNotBlank() }

    private data class FavoriteUiState(
        val canUseWishlist: Boolean,
        val favoriteMessage: String?,
        val pendingFavoriteIds: Set<String>,
    )
}
