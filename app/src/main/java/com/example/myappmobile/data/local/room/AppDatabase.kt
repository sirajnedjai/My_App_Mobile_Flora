package com.example.myappmobile.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myappmobile.data.local.room.dao.ProductDao
import com.example.myappmobile.data.local.room.dao.UserDao
import com.example.myappmobile.data.local.room.entity.ProductEntity
import com.example.myappmobile.data.local.room.entity.UserEntity

@Database(
    entities = [UserEntity::class, ProductEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
}
