package com.example.myappmobile.data.mapper

import com.example.myappmobile.domain.Product
import com.example.myappmobile.domain.model.CartItem

object ProductMapper {
    fun map(product: Product): com.example.myappmobile.domain.model.Product =
        com.example.myappmobile.domain.model.Product(
            id = product.id,
            name = product.name,
            price = product.price,
            imageUrl = product.imageUrl,
            studio = product.studio,
            category = product.category.name,
            isFavorited = product.isFavorited,
            collectionLabel = product.category.name,
            images = listOf(product.imageUrl),
        )

    fun toCartItem(product: Product): CartItem = CartItem(
        id = "cart_${product.id}",
        product = map(product),
        quantity = 1,
    )

    fun toSellerProduct(product: Product): com.example.myappmobile.domain.model.Product =
        com.example.myappmobile.domain.model.Product(
            id = product.id,
            name = product.name,
            price = product.price,
            imageUrl = product.imageUrl,
            studio = product.studio,
            storeId = if (product.studio.contains("FLORA", ignoreCase = true)) "s1" else "",
            category = product.category.name,
            description = product.tags.joinToString().ifBlank { "Handcrafted atelier piece." },
            isFavorited = product.isFavorited,
            images = listOf(product.imageUrl),
        )
}
