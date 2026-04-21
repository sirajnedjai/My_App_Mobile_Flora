package com.example.myappmobile.domain.model

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val studio: String,
    val storeId: String = "",
    val category: String = "",
    val description: String = "",
    val material: String = "",
    val dimensions: String = "",
    val stockCount: Int = 0,
    val isFavorited: Boolean = false,
    val isLimitedEdition: Boolean = false,
    val tags: List<String> = emptyList(),
    val variants: List<ProductVariant> = emptyList(),
    val collectionLabel: String = "",
    val story: String = "",
    val images: List<String> = emptyList(),
    val status: String = "",
)

data class ProductVariant(
    val id: String,
    val palette: String = "",
    val size: String = "",
    val availability: String = "",
)

data class SellerManagedProductDetails(
    val product: Product,
    val status: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val sellerName: String = "",
)
