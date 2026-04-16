package com.example.myappmobile.presentation.seller.manageproducts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.domain.model.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SellerProductManagementViewModel : ViewModel() {

    private val authRepository = AppContainer.authRepository
    private val repository = AppContainer.sellerManagementRepository
    private val editorState = MutableStateFlow(SellerProductManagementUiState())

    val uiState: StateFlow<SellerProductManagementUiState> = combine(
        authRepository.currentUser,
        authRepository.currentUser.flatMapLatest { user ->
            repository.getProductsForSeller(user.id)
        },
        editorState,
    ) { user, products, editor ->
        editor.copy(
            user = user,
            isSeller = user.isSeller,
            products = if (user.isSeller) products else emptyList(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SellerProductManagementUiState(),
    )

    fun showAddProductDialog() {
        editorState.value = SellerProductManagementUiState(
            isEditorVisible = true,
            imageUrl = DEFAULT_PRODUCT_IMAGE,
            stockCount = "1",
        )
    }

    fun editProduct(product: Product) {
        editorState.value = SellerProductManagementUiState(
            isEditorVisible = true,
            editingProductId = product.id,
            name = product.name,
            price = product.price.toString(),
            category = product.category,
            description = product.description,
            imageUrl = product.imageUrl,
            stockCount = product.stockCount.toString(),
        )
    }

    fun dismissEditor() {
        editorState.update {
            it.copy(
                isEditorVisible = false,
                isSaving = false,
                editingProductId = null,
                formError = null,
            )
        }
    }

    fun onNameChanged(value: String) {
        editorState.update { it.copy(name = value, formError = null) }
    }

    fun onPriceChanged(value: String) {
        editorState.update {
            it.copy(
                price = value.filter { character -> character.isDigit() || character == '.' },
                formError = null,
            )
        }
    }

    fun onCategoryChanged(value: String) {
        editorState.update { it.copy(category = value, formError = null) }
    }

    fun onDescriptionChanged(value: String) {
        editorState.update { it.copy(description = value, formError = null) }
    }

    fun onImageUrlChanged(value: String) {
        editorState.update { it.copy(imageUrl = value, formError = null) }
    }

    fun onStockCountChanged(value: String) {
        editorState.update { it.copy(stockCount = value.filter(Char::isDigit), formError = null) }
    }

    fun requestDelete(product: Product) {
        editorState.update { it.copy(pendingDelete = product) }
    }

    fun dismissDelete() {
        editorState.update { it.copy(pendingDelete = null) }
    }

    fun saveProduct() {
        val state = uiState.value
        val user = state.user ?: return
        if (!user.isSeller) return

        val trimmedName = state.name.trim()
        val trimmedCategory = state.category.trim()
        val trimmedDescription = state.description.trim()
        val parsedPrice = state.price.toDoubleOrNull()
        val stockCount = state.stockCount.toIntOrNull() ?: 0

        val validationError = when {
            trimmedName.isBlank() -> "Enter a product name."
            trimmedDescription.isBlank() -> "Add a short description so buyers understand the piece."
            trimmedCategory.isBlank() -> "Choose a category for this product."
            parsedPrice == null || parsedPrice <= 0.0 -> "Enter a valid price."
            stockCount < 0 -> "Stock cannot be negative."
            else -> null
        }

        if (validationError != null) {
            editorState.update { it.copy(formError = validationError) }
            return
        }

        viewModelScope.launch {
            editorState.update { it.copy(isSaving = true, formError = null) }
            repository.upsertProduct(
                sellerId = user.id,
                productId = state.editingProductId,
                name = trimmedName,
                price = requireNotNull(parsedPrice),
                category = trimmedCategory,
                description = trimmedDescription,
                imageUrl = state.imageUrl.trim(),
                stockCount = stockCount,
            )
            editorState.value = SellerProductManagementUiState()
        }
    }

    fun deleteProduct() {
        val state = uiState.value
        val user = state.user ?: return
        val target = state.pendingDelete ?: return
        if (!user.isSeller) return

        viewModelScope.launch {
            repository.deleteProduct(user.id, target.id)
            editorState.update { it.copy(pendingDelete = null) }
        }
    }

    private companion object {
        const val DEFAULT_PRODUCT_IMAGE =
            "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800"
    }
}
