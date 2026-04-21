package com.example.myappmobile.data.local.room

import android.util.Log
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myappmobile.data.local.room.entity.ProductEntity
import com.example.myappmobile.data.local.room.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object DatabaseProvider {
    private const val DATABASE_NAME = "flora_mvp.db"
    private const val TAG = "DatabaseProvider"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var database: AppDatabase? = null
    @Volatile
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
        if (database != null) return

        synchronized(this) {
            appContext = context.applicationContext
            if (database != null) return
            Log.d(TAG, "Initializing Room database")

            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME,
            ).addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                .build()

            database = instance

            scope.launch {
                runCatching {
                    if (instance.productDao().countProducts() == 0) {
                        instance.productDao().insertAll(defaultProducts)
                    }
                    if (instance.userDao().countUsers() == 0) {
                        defaultUsers.forEach { user ->
                            instance.userDao().insert(user)
                        }
                    }
                }.onFailure { throwable ->
                    Log.e(TAG, "Room initialization seed failed", throwable)
                }
            }
            Log.d(TAG, "Room database initialized")
        }
    }

    fun getDatabase(): AppDatabase {
        database?.let { return it }
        appContext?.let { context ->
            Log.d(TAG, "Database requested before explicit initialization. Initializing from stored application context.")
            initialize(context)
        }
        Log.d(TAG, "Returning Room database instance. initialized=${database != null}")
        return checkNotNull(database) {
            "DatabaseProvider is not initialized. Call DatabaseProvider.initialize(context) first."
        }
    }

    fun isInitialized(): Boolean = database != null

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS products_new (
                    id TEXT NOT NULL,
                    sellerId TEXT NOT NULL DEFAULT '',
                    name TEXT NOT NULL,
                    description TEXT NOT NULL,
                    price REAL NOT NULL,
                    imageUrl TEXT NOT NULL,
                    category TEXT NOT NULL DEFAULT '',
                    studio TEXT NOT NULL DEFAULT '',
                    stockCount INTEGER NOT NULL DEFAULT 0,
                    isFavorited INTEGER NOT NULL DEFAULT 0,
                    isFeatured INTEGER NOT NULL DEFAULT 0,
                    isNewArrival INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(id)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO products_new (
                    id,
                    sellerId,
                    name,
                    description,
                    price,
                    imageUrl,
                    category,
                    studio,
                    stockCount,
                    isFavorited,
                    isFeatured,
                    isNewArrival,
                    createdAt
                )
                SELECT
                    'legacy_' || id,
                    '',
                    name,
                    description,
                    price,
                    imageUrl,
                    COALESCE(category, ''),
                    'FLORA Atelier',
                    0,
                    0,
                    0,
                    0,
                    id
                FROM products
                """.trimIndent()
            )
            db.execSQL("DROP TABLE products")
            db.execSQL("ALTER TABLE products_new RENAME TO products")
        }
    }

    private val defaultProducts = listOf(
        ProductEntity(
            id = "flora_seed_1",
            sellerId = "1",
            name = "Handwoven Luna Bag",
            description = "A handmade woven bag with soft neutral tones for everyday elegance.",
            price = 78.0,
            imageUrl = "https://images.unsplash.com/photo-1584917865442-de89df76afd3",
            category = "Accessories",
            studio = "FLORA Atelier",
            isFeatured = true,
            createdAt = 1L,
        ),
        ProductEntity(
            id = "flora_seed_2",
            sellerId = "",
            name = "Rose Garden Dress",
            description = "A floral artisan dress cut in a flowing silhouette.",
            price = 112.0,
            imageUrl = "https://images.unsplash.com/photo-1496747611176-843222e1e57c",
            category = "Handmade",
            studio = "Maison Rosette",
            isNewArrival = true,
            createdAt = 2L,
        ),
        ProductEntity(
            id = "flora_seed_3",
            sellerId = "",
            name = "Botanical Silk Scarf",
            description = "Printed silk scarf inspired by fresh spring blossoms.",
            price = 54.0,
            imageUrl = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab",
            category = "Accessories",
            studio = "ELF-READ Textile",
            isFeatured = true,
            createdAt = 3L,
        ),
        ProductEntity(
            id = "flora_seed_4",
            sellerId = "",
            name = "Natural Rose Soap",
            description = "Cold-processed soap bar with rose petals and shea butter.",
            price = 14.5,
            imageUrl = "https://images.unsplash.com/photo-1607006483225-4d6c0f3f7fd7",
            category = "Beauty",
            studio = "Petal Rituals",
            createdAt = 4L,
        ),
        ProductEntity(
            id = "flora_seed_5",
            sellerId = "",
            name = "Amber Clay Mask",
            description = "A small-batch face mask with clay, honey, and floral oils.",
            price = 29.0,
            imageUrl = "https://images.unsplash.com/photo-1570172619644-dfd03ed5d881",
            category = "Beauty",
            studio = "Petal Rituals",
            isNewArrival = true,
            createdAt = 5L,
        ),
        ProductEntity(
            id = "flora_seed_6",
            sellerId = "",
            name = "Pearl Bloom Earrings",
            description = "Delicate handmade earrings with pearl-inspired detailing.",
            price = 46.0,
            imageUrl = "https://images.unsplash.com/photo-1617038220319-276d3cfab638",
            category = "Jewelry",
            studio = "AURUM Craft",
            isFeatured = true,
            createdAt = 6L,
        ),
        ProductEntity(
            id = "flora_seed_7",
            sellerId = "",
            name = "Gold Leaf Bracelet",
            description = "Slim artisan bracelet with warm gold leaf accents.",
            price = 38.0,
            imageUrl = "https://images.unsplash.com/photo-1611591437281-460bfbe1220a",
            category = "Jewelry",
            studio = "AURUM Craft",
            createdAt = 7L,
        ),
        ProductEntity(
            id = "flora_seed_8",
            sellerId = "",
            name = "Linen Blossom Blouse",
            description = "Breathable linen blouse stitched for soft everyday luxury.",
            price = 67.0,
            imageUrl = "https://images.unsplash.com/photo-1529139574466-a303027c1d8b",
            category = "Handmade",
            studio = "Maison Rosette",
            createdAt = 8L,
        ),
        ProductEntity(
            id = "flora_seed_9",
            sellerId = "",
            name = "Cedar Wooden Tray",
            description = "Minimal artisan tray handcrafted for decor and serving.",
            price = 49.0,
            imageUrl = "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85",
            category = "Decor",
            studio = "Oak & Brass",
            createdAt = 9L,
        ),
        ProductEntity(
            id = "flora_seed_10",
            sellerId = "1",
            name = "Ceramic Bloom Vase",
            description = "Matte ceramic vase shaped for fresh or dried arrangements.",
            price = 58.0,
            imageUrl = "https://images.unsplash.com/photo-1517705008128-361805f42e86",
            category = "Decor",
            studio = "FLORA Ceramics",
            stockCount = 9,
            isFeatured = true,
            createdAt = 10L,
        ),
        ProductEntity(
            id = "flora_seed_11",
            sellerId = "",
            name = "Honey Almond Granola",
            description = "Small-batch granola made with roasted almonds and honey.",
            price = 18.0,
            imageUrl = "https://images.unsplash.com/photo-1514996937319-344454492b37",
            category = "Food",
            studio = "Harvest Table",
            createdAt = 11L,
        ),
        ProductEntity(
            id = "flora_seed_12",
            sellerId = "",
            name = "Herbal Tea Collection",
            description = "A calming selection of floral and citrus herbal teas.",
            price = 22.0,
            imageUrl = "https://images.unsplash.com/photo-1507914372368-b2b085b925a1",
            category = "Food",
            studio = "Harvest Table",
            isNewArrival = true,
            createdAt = 12L,
        ),
        ProductEntity(
            id = "flora_seed_13",
            sellerId = "",
            name = "Velvet Evening Clutch",
            description = "Compact handcrafted clutch in a deep warm bronze shade.",
            price = 83.0,
            imageUrl = "https://images.unsplash.com/photo-1594633312681-425c7b97ccd1",
            category = "Accessories",
            studio = "Atelier Bronze",
            createdAt = 13L,
        ),
        ProductEntity(
            id = "flora_seed_14",
            sellerId = "",
            name = "Shea Blossom Balm",
            description = "Nourishing body balm made with shea, cocoa, and floral oils.",
            price = 24.0,
            imageUrl = "https://images.unsplash.com/photo-1556228578-8c89e6adf883",
            category = "Beauty",
            studio = "Petal Rituals",
            createdAt = 14L,
        ),
        ProductEntity(
            id = "flora_seed_15",
            sellerId = "1",
            name = "Charcoal Pitcher No. 14",
            description = "Hand-thrown stoneware with a matte charcoal glaze and sculpted silhouette.",
            price = 240.0,
            imageUrl = "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800",
            category = "Ceramics",
            studio = "FLORA Ceramics",
            stockCount = 12,
            isFeatured = true,
            createdAt = 15L,
        ),
        ProductEntity(
            id = "flora_seed_16",
            sellerId = "1",
            name = "Nocturne Scented Vessel",
            description = "A hand-poured amberwood candle housed in a reusable clay vessel.",
            price = 95.0,
            imageUrl = "https://images.unsplash.com/photo-1608501078713-8e445a709b39?w=800",
            category = "Ceramics",
            studio = "FLORA Ceramics",
            stockCount = 28,
            isNewArrival = true,
            createdAt = 16L,
        ),
    )

    private val defaultUsers = listOf(
        UserEntity(
            name = "Sienna Moretti",
            email = "sienna@flora.com",
            password = "password123",
            phone = "+39 055 234-8890",
            role = "Seller",
        ),
        UserEntity(
            name = "Elena Vance",
            email = "elena.vance@flora.com",
            password = "password123",
            phone = "+1 (555) 300-4400",
            role = "Customer",
        ),
        UserEntity(
            name = "Julianne Moore",
            email = "julianne@flora.com",
            password = "password123",
            phone = "+1 (555) 100-2020",
            role = "Customer",
        ),
    )
}
