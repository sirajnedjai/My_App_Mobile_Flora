package com.example.myappmobile.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val sellerId: String = "",
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String = "",
    val studio: String = "",
    val stockCount: Int = 0,
    val isFavorited: Boolean = false,
    val isFeatured: Boolean = false,
    val isNewArrival: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
