package com.example.myappmobile.data.mapper

import com.example.myappmobile.data.local.room.entity.ProductEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductEntityMapperTest {

    @Test
    fun categoryId_normalizesHumanReadableCategory() {
        assertEquals("home_and_living", ProductEntityMapper.categoryId("Home & Living"))
    }

    @Test
    fun toDomainProduct_preservesSellerEditedMetadata() {
        val entity = ProductEntity(
            id = "seller_1",
            sellerId = "1",
            name = "Amber Vessel",
            description = "A warm ceramic accent",
            price = 110.0,
            imageUrl = "https://example.com/item.jpg",
            category = "Ceramics",
            studio = "FLORA Ceramics",
            stockCount = 2,
            isFavorited = true,
        )

        val product = ProductEntityMapper.toDomainProduct(entity)

        assertEquals("seller_1", product.id)
        assertEquals("FLORA Ceramics", product.studio)
        assertEquals("ceramics", product.category.id)
        assertTrue(product.isFavorited)
        assertTrue(product.tags.contains("Seller Edit"))
    }
}
