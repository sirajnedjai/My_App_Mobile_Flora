package com.example.myappmobile.data.repository

import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.data.local.room.DatabaseProvider
import com.example.myappmobile.data.local.room.entity.ProductEntity
import com.example.myappmobile.data.mapper.ProductEntityMapper
import com.example.myappmobile.domain.Product
import com.example.myappmobile.domain.ProductDetails
import com.example.myappmobile.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl : ProductRepository {

    private val productDao by lazy { DatabaseProvider.getDatabase().productDao() }

    override fun observeAllProducts(): Flow<List<Product>> = productDao.getAll().map { products ->
        products.map(ProductEntityMapper::toDomainProduct)
    }

    override suspend fun getFeaturedProducts(): List<Product> = productDao.getAllOnce()
        .filter(ProductEntity::isFeatured)
        .map(ProductEntityMapper::toDomainProduct)
        .ifEmpty { getAllProducts().take(4) }

    override suspend fun getNewArrivals(): List<Product> = productDao.getAllOnce()
        .filter(ProductEntity::isNewArrival)
        .sortedByDescending(ProductEntity::createdAt)
        .map(ProductEntityMapper::toDomainProduct)
        .ifEmpty { getAllProducts().takeLast(4) }

    override suspend fun getAllProducts(): List<Product> = observeAllProducts().first()

    override suspend fun searchProducts(query: String): List<Product> = if (query.isBlank()) {
        getAllProducts()
    } else {
        getAllProducts().filter { product ->
            product.name.contains(query, ignoreCase = true) ||
                product.studio.contains(query, ignoreCase = true) ||
                product.category.name.contains(query, ignoreCase = true)
        }
    }

    override suspend fun getProductDetails(productId: String): ProductDetails {
        val catalog = productDao.getAllOnce()
        val target = catalog.firstOrNull { it.id == productId } ?: catalog.firstOrNull() ?: ProductEntity(
            id = productId,
            name = "FLORA Piece",
            description = "A handcrafted atelier selection.",
            price = 0.0,
            imageUrl = "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800",
            category = "Curated",
            studio = "FLORA Atelier",
        )
        val sameCategory = catalog
            .filter { it.id != target.id && it.category.equals(target.category, ignoreCase = true) }
        val fallback = catalog
            .filter { it.id != target.id && !it.category.equals(target.category, ignoreCase = true) }
        val similar = (sameCategory + fallback)
            .distinctBy(ProductEntity::id)
            .take(6)
            .map(ProductEntityMapper::toDomainProduct)
        val baseDetails = ProductEntityMapper.toDetails(target, similar)
        val storeConfiguration = AppContainer.uiPreferencesRepository.getStoreConfiguration(target.sellerId)
        val sellerProfile = AppContainer.uiPreferencesRepository.getAccountProfile(target.sellerId)
        return baseDetails.copy(
            artist = baseDetails.artist.copy(
                name = storeConfiguration.ownerName.ifBlank {
                    sellerProfile.fullName.ifBlank { baseDetails.artist.name }
                },
                avatarUrl = sellerProfile.avatarUri.ifBlank {
                    storeConfiguration.logoUri.ifBlank { baseDetails.artist.avatarUrl }
                },
                studioName = storeConfiguration.shopName.ifBlank { target.studio.ifBlank { baseDetails.artist.studioName } },
                sellerApprovalStatus = AppContainer.uiPreferencesRepository.getSellerApprovalStatus(target.sellerId),
            ),
        )
    }

    override suspend fun toggleFavorite(productId: String) {
        val existing = productDao.getById(productId) ?: return
        productDao.insert(existing.copy(isFavorited = !existing.isFavorited))
    }

    override suspend fun getFavoriteProducts(): List<Product> = productDao.getAllOnce()
        .filter(ProductEntity::isFavorited)
        .map(ProductEntityMapper::toDomainProduct)
}
