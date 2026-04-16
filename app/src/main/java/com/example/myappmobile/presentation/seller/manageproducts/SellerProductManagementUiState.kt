package com.example.myappmobile.presentation.seller.manageproducts

import com.example.myappmobile.domain.model.Product
import com.example.myappmobile.domain.model.User

data class SellerProductManagementUiState(
    val user: User? = null,
    val isSeller: Boolean = false,
    val products: List<Product> = emptyList(),
    val isEditorVisible: Boolean = false,
    val isSaving: Boolean = false,
    val editingProductId: String? = null,
    val name: String = "",
    val price: String = "",
    val category: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val stockCount: String = "",
    val formError: String? = null,
    val pendingDelete: Product? = null,
)
