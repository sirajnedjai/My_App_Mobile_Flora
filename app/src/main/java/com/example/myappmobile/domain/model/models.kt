package com.example.myappmobile.domain

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val studio: String,
    val category: Category,
    val isFavorited: Boolean = false,
    val tags: List<String> = emptyList(),
)

data class ProductDetails(
    val id: String,
    val name: String,
    val collectionLabel: String,
    val price: Double,
    val story: String,
    val material: String,
    val dimensions: String,
    val images: List<String>,
    val artist: ArtistProfile,
    val reviews: List<Review>,
    val similarProducts: List<Product>,
    val isFavorited: Boolean = false,
)

data class ArtistProfile(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val rating: Float,
    val reviewCount: Int,
    val studioName: String,
)

data class Review(
    val id: String,
    val authorName: String,
    val rating: Int,
    val text: String,
    val isVerified: Boolean,
)

data class Category(
    val id: String,
    val name: String,
    val iconRes: Int,
)

data class BannerData(
    val title: String,
    val subtitle: String,
    val ctaText: String,
    val imageUrl: String,
)