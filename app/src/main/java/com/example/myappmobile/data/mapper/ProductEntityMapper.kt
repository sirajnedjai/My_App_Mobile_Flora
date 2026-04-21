package com.example.myappmobile.data.mapper

import com.example.myappmobile.data.local.room.entity.ProductEntity
import com.example.myappmobile.domain.ArtistProfile
import com.example.myappmobile.domain.Category
import com.example.myappmobile.domain.Product
import com.example.myappmobile.domain.ProductDetails

object ProductEntityMapper {

    fun toDomainProduct(entity: ProductEntity): Product = Product(
        id = entity.id,
        name = entity.name,
        price = entity.price,
        imageUrl = entity.imageUrl,
        studio = entity.studio.ifBlank { "FLORA Atelier" },
        category = Category(
            id = categoryId(entity.category),
            name = entity.category.ifBlank { "Curated" },
            iconRes = android.R.drawable.ic_menu_gallery,
        ),
        isFavorited = entity.isFavorited,
        tags = buildList {
            if (entity.sellerId.isNotBlank()) add("Seller Edit")
            if (entity.stockCount <= 3) add("Limited")
        },
    )

    fun toDetails(entity: ProductEntity, similarProducts: List<Product>): ProductDetails {
        val product = toDomainProduct(entity)
        return ProductDetails(
            id = product.id,
            sellerId = entity.sellerId,
            name = product.name,
            collectionLabel = product.category.name.uppercase(),
            price = product.price,
            story = entity.description.ifBlank {
                "A handcrafted FLORA piece created in small batches with an emphasis on material warmth and quiet luxury."
            },
            material = "Artisan-crafted finish",
            dimensions = "Details available on request",
            images = detailImagesFor(product.imageUrl),
            artist = ArtistProfile(
                id = entity.sellerId.ifBlank { "flora_guest" },
                name = product.studio,
                avatarUrl = product.imageUrl,
                rating = 4.9f,
                reviewCount = 48,
                studioName = "Visit Studio",
            ),
            reviews = emptyList(),
            similarProducts = similarProducts,
            isFavorited = product.isFavorited,
        )
    }

    fun categoryId(category: String): String = category
        .trim()
        .lowercase()
        .replace("&", "and")
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
        .ifBlank { "curated" }

    private fun detailImagesFor(primaryImageUrl: String): List<String> =
        listOf(primaryImageUrl).filter { it.isNotBlank() }
}
