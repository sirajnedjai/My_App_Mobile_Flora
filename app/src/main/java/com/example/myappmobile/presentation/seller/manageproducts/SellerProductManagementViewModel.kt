package com.example.myappmobile.presentation.seller.manageproducts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.remote.ApiException
import com.example.myappmobile.data.remote.toApiException
import com.example.myappmobile.domain.model.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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

    init {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                if (!user.isSeller) {
                    editorState.update { it.copy(isLoading = false, errorMessage = null) }
                    return@collectLatest
                }
                Log.d(TAG, "Refreshing seller studio for userId=${user.id} isSeller=${user.isSeller}")
                editorState.update { it.copy(isLoading = true, errorMessage = null) }
                repository.refreshProductsForSeller(user.id).fold(
                    onSuccess = {
                        editorState.update { it.copy(isLoading = false, errorMessage = null) }
                    },
                    onFailure = { error ->
                        editorState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.toApiException().message,
                            )
                        }
                    },
                )
            }
        }
    }

    val uiState: StateFlow<SellerProductManagementUiState> = combine(
        authRepository.currentUser,
        authRepository.currentUser.flatMapLatest { user ->
            if (user.isSeller) {
                repository.getProductsForSeller(user.id)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
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
            user = uiState.value.user,
            isSeller = uiState.value.isSeller,
            products = uiState.value.products,
            isLoading = uiState.value.isLoading,
            isEditorVisible = true,
            status = SellerProductStatus.ACTIVE.value,
            stockCount = "1",
            errorMessage = uiState.value.errorMessage,
        )
    }

    fun editProduct(product: Product) {
        editorState.value = SellerProductManagementUiState(
            user = uiState.value.user,
            isSeller = uiState.value.isSeller,
            products = uiState.value.products,
            isLoading = uiState.value.isLoading,
            isEditorVisible = true,
            editingProductId = product.id,
            name = product.name,
            price = product.price.toString(),
            category = product.category,
            description = product.description,
            status = SellerProductStatus.fromValue(product.status).value,
            existingImageUrl = product.imageUrl,
            stockCount = product.stockCount.toString(),
            errorMessage = uiState.value.errorMessage,
        )
    }

    fun dismissEditor() {
        editorState.update {
            it.copy(
                isEditorVisible = false,
                isSaving = false,
                editingProductId = null,
                existingImageUrl = "",
                selectedImageUri = "",
                selectedImageLabel = "",
                status = SellerProductStatus.ACTIVE.value,
                fieldErrors = SellerProductFieldErrors(),
                formError = null,
            )
        }
    }

    fun onNameChanged(value: String) {
        editorState.update { it.copy(name = value, formError = null, fieldErrors = it.fieldErrors.copy(name = null)) }
    }

    fun onPriceChanged(value: String) {
        editorState.update {
            it.copy(
                price = value.filter { character -> character.isDigit() || character == '.' },
                formError = null,
                fieldErrors = it.fieldErrors.copy(price = null),
            )
        }
    }

    fun onCategoryChanged(value: String) {
        editorState.update { it.copy(category = value, formError = null, fieldErrors = it.fieldErrors.copy(category = null)) }
    }

    fun onDescriptionChanged(value: String) {
        editorState.update { it.copy(description = value, formError = null, fieldErrors = it.fieldErrors.copy(description = null)) }
    }

    fun onStatusChanged(value: String) {
        editorState.update { it.copy(status = value, formError = null, fieldErrors = it.fieldErrors.copy(status = null)) }
    }

    fun onImageSelected(uri: String, label: String) {
        Log.d(TAG, "New seller product image selected. editingProductId=${uiState.value.editingProductId.orEmpty()} uri=$uri label=$label")
        editorState.update {
            it.copy(
                selectedImageUri = uri,
                selectedImageLabel = label,
                formError = null,
                fieldErrors = it.fieldErrors.copy(imageFile = null),
            )
        }
    }

    fun clearSelectedImage() {
        editorState.update {
            it.copy(
                selectedImageUri = "",
                selectedImageLabel = "",
                fieldErrors = it.fieldErrors.copy(imageFile = null),
            )
        }
    }

    fun onStockCountChanged(value: String) {
        editorState.update {
            it.copy(
                stockCount = value.filter(Char::isDigit),
                formError = null,
                fieldErrors = it.fieldErrors.copy(stock = null),
            )
        }
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

        val fieldErrors = SellerProductFieldErrors(
            name = "Enter a product name.".takeIf { trimmedName.isBlank() },
            price = "Enter a valid price.".takeIf { parsedPrice == null || parsedPrice < 0.0 },
            category = "Choose a category for this product.".takeIf { trimmedCategory.isBlank() },
            stock = "Stock cannot be negative.".takeIf { state.stockCount.isBlank() },
            status = "Select a valid product status.".takeIf {
                SellerProductStatus.entries.none { status -> status.value == state.status }
            },
        )

        if (fieldErrors.hasErrors()) {
            editorState.update { it.copy(fieldErrors = fieldErrors, formError = null) }
            return
        }

        viewModelScope.launch {
            editorState.update { it.copy(isSaving = true, formError = null, fieldErrors = SellerProductFieldErrors()) }
            Log.d(
                TAG,
                "Submitting seller product save for ${state.editingProductId ?: "new"} userId=${user.id} isSeller=${user.isSeller} status=${state.status} category=${trimmedCategory} imageSelected=${state.selectedImageUri.isNotBlank()}",
            )
            repository.upsertProduct(
                sellerId = user.id,
                productId = state.editingProductId,
                name = trimmedName,
                price = requireNotNull(parsedPrice),
                category = trimmedCategory,
                description = trimmedDescription,
                status = state.status,
                imageUri = state.selectedImageUri.ifBlank { null },
                stockCount = stockCount,
            ).fold(
                onSuccess = {
                    Log.d(
                        TAG,
                        "Seller product save completed. productId=${state.editingProductId.orEmpty()} name=$trimmedName",
                    )
                    editorState.value = SellerProductManagementUiState()
                },
                onFailure = { error ->
                    val apiError = error.toApiException()
                    Log.d(TAG, "Seller product save failed. message=${apiError.message} validation=${apiError.validationErrors}")
                    editorState.update {
                        it.copy(
                            isSaving = false,
                            formError = apiError.message,
                            fieldErrors = apiError.toFieldErrors(),
                        )
                    }
                },
            )
        }
    }

    fun deleteProduct() {
        val state = uiState.value
        val user = state.user ?: return
        val target = state.pendingDelete ?: return
        if (!user.isSeller) return

        viewModelScope.launch {
            editorState.update { it.copy(isDeleting = true, errorMessage = null) }
            Log.d(TAG, "Deleting seller product ${target.id} userId=${user.id} isSeller=${user.isSeller}")
            repository.deleteProduct(user.id, target.id).fold(
                onSuccess = {
                    editorState.update { it.copy(pendingDelete = null, errorMessage = null, isDeleting = false) }
                },
                onFailure = { error ->
                    val apiError = error.toApiException()
                    Log.d(TAG, "Seller product delete failed. message=${apiError.message}")
                    editorState.update {
                        it.copy(
                            pendingDelete = null,
                            errorMessage = apiError.message,
                            isDeleting = false,
                        )
                    }
                },
            )
        }
    }

    private fun SellerProductFieldErrors.hasErrors(): Boolean =
        name != null || description != null || price != null || category != null || stock != null || status != null || imageFile != null

    private fun ApiException.toFieldErrors(): SellerProductFieldErrors = SellerProductFieldErrors(
        name = validationErrors["name"]?.firstOrNull(),
        description = validationErrors["description"]?.firstOrNull(),
        price = validationErrors["price"]?.firstOrNull(),
        category = validationErrors["category"]?.firstOrNull(),
        stock = validationErrors["stock"]?.firstOrNull(),
        status = validationErrors["status"]?.firstOrNull(),
        imageFile = validationErrors["image_file"]?.firstOrNull(),
    )

    private companion object {
        const val TAG = "SellerProductMgmtVM"
    }
}
