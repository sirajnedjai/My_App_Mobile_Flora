package com.example.myappmobile.presentation.seller.manageproducts

import com.example.myappmobile.domain.model.Product
import com.example.myappmobile.domain.model.User

data class SellerProductManagementUiState(
    val user: User? = null,
    val isSeller: Boolean = false,
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val isEditorVisible: Boolean = false,
    val isSaving: Boolean = false,
    val editingProductId: String? = null,
    val name: String = "",
    val price: String = "",
    val category: String = "",
    val description: String = "",
    val status: String = SellerProductStatus.ACTIVE.value,
    val existingImageUrl: String = "",
    val selectedImageUri: String = "",
    val selectedImageLabel: String = "",
    val stockCount: String = "",
    val fieldErrors: SellerProductFieldErrors = SellerProductFieldErrors(),
    val formError: String? = null,
    val errorMessage: String? = null,
    val pendingDelete: Product? = null,
    val isDeleting: Boolean = false,
)

data class SellerProductFieldErrors(
    val name: String? = null,
    val description: String? = null,
    val price: String? = null,
    val category: String? = null,
    val stock: String? = null,
    val status: String? = null,
    val imageFile: String? = null,
)

enum class SellerProductStatus(val value: String, val label: String) {
    ACTIVE("active", "Active"),
    INACTIVE("inactive", "Inactive");

    companion object {
        fun fromValue(value: String): SellerProductStatus =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: ACTIVE
    }
}
