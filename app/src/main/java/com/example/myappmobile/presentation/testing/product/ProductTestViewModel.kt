package com.example.myappmobile.presentation.testing.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.data.local.room.DatabaseProvider
import com.example.myappmobile.data.local.room.entity.ProductEntity
import com.example.myappmobile.data.local.room.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductTestUiState(
    val isLoading: Boolean = true,
    val products: List<ProductEntity> = emptyList(),
)

class ProductTestViewModel : ViewModel() {
    private val repository = ProductRepository(DatabaseProvider.getDatabase().productDao())
    private val loading = MutableStateFlow(true)

    val uiState: StateFlow<ProductTestUiState> = combine(
        repository.getAllProducts(),
        loading,
    ) { products, isLoading ->
        ProductTestUiState(
            isLoading = isLoading && products.isEmpty(),
            products = products,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProductTestUiState(),
    )

    init {
        viewModelScope.launch {
            loading.value = false
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProductById(product.id)
        }
    }
}
