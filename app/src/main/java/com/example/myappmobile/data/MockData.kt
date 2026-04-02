package com.example.myappmobile.data

import com.example.myappmobile.domain.*

object MockData {

    val categories = listOf(
        Category("jewelry", "JEWELRY", android.R.drawable.ic_menu_gallery),
        Category("home", "HOME", android.R.drawable.ic_menu_gallery),
        Category("textiles", "TEXTILES", android.R.drawable.ic_menu_gallery),
        Category("ceramics", "CERAMICS", android.R.drawable.ic_menu_gallery),
    )

    val banner = BannerData(
        title = "The Art of Craft",
        subtitle = "Discover a curated collection of handmade objects designed for the modern woman.",
        ctaText = "EXPLORE COLLECTION",
        imageUrl = "https://images.unsplash.com/photo-1595438958485-8f1a0fece77e?w=800",
    )

    private val ceramicsCategory = categories[3]
    private val jewelryCategory = categories[0]
    private val textilesCategory = categories[2]
    private val homeCategory = categories[1]

    val featuredProducts = listOf(
        Product(
            id = "fp1",
            name = "The Sculptural Vessel",
            price = 240.0,
            imageUrl = "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=600",
            studio = "METUDIO CERAMICS",
            category = ceramicsCategory,
            isFavorited = true,
        ),
        Product(
            id = "fp2",
            name = "Solar Pearl Necklace",
            price = 185.0,
            imageUrl = "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=600",
            studio = "SUBLIME NYC",
            category = jewelryCategory,
        ),
        Product(
            id = "fp3",
            name = "Woven Earth Throw",
            price = 320.0,
            imageUrl = "https://images.unsplash.com/photo-1617952739429-b4f8a52ccad6?w=600",
            studio = "LOOM & WEFT",
            category = textilesCategory,
        ),
    )

    val newArrivals = listOf(
        Product(
            id = "na1",
            name = "Tempus Minimalist",
            price = 319.0,
            imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600",
            studio = "TEMPUS CO.",
            category = homeCategory,
        ),
        Product(
            id = "na2",
            name = "Obsidian Ring",
            price = 145.0,
            imageUrl = "https://images.unsplash.com/photo-1605100804763-247f67b3557e?w=600",
            studio = "FORGED ATELIER",
            category = jewelryCategory,
        ),
    )

    val artist = ArtistProfile(
        id = "a1",
        name = "Sienna Moretti",
        avatarUrl = "https://images.unsplash.com/photo-1494790108755-2616b612b47c?w=100",
        rating = 4.9f,
        reviewCount = 124,
        studioName = "Visit Studio",
    )

    val reviews = listOf(
        Review(
            id = "r1",
            authorName = "JULIAN H — VERIFIED COLLECTOR",
            rating = 5,
            text = "\"The texture is even more beautiful in person. It has a weight and presence that grounds my entire living room.\"",
            isVerified = true,
        ),
        Review(
            id = "r2",
            authorName = "ELENA S — VERIFIED COLLECTOR",
            rating = 5,
            text = "\"A true masterpiece of craftsmanship. The glaze catching the light at sunset is magical.\"",
            isVerified = true,
        ),
        Review(
            id = "r3",
            authorName = "MARGOT T — VERIFIED COLLECTOR",
            rating = 5,
            text = "\"Arrived perfectly packaged. The organic shape makes it feel like it was grown rather than made.\"",
            isVerified = true,
        ),
    )

    val sculptedRippleVase = ProductDetails(
        id = "prv1",
        name = "The Sculpted\nRipple Vase",
        collectionLabel = "SPRING COLLECTION '24",
        price = 185.0,
        story = "Each piece is hand-thrown in our coastal workshop, capturing the rhythmic movement of the tide. The unglazed exterior celebrates the raw beauty of toasted stoneware, while the interior is finished in a smooth milk-white glaze.",
        material = "Toasted Stoneware",
        dimensions = "9\" H × 6\" W",
        images = listOf(
            "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800",
            "https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=400",
            "https://images.unsplash.com/photo-1612198188060-c7c2a3b66eae?w=400",
        ),
        artist = artist,
        reviews = reviews,
        similarProducts = listOf(
            Product(
                id = "sim1",
                name = "Basalt Tea Set",
                price = 320.0,
                imageUrl = "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=400",
                studio = "STONE FORM",
                category = ceramicsCategory,
            ),
            Product(
                id = "sim2",
                name = "Earth Bowl",
                price = 88.0,
                imageUrl = "https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=400",
                studio = "CLAY & CO",
                category = ceramicsCategory,
            ),
        ),
    )

    private val allProductsInternal = buildList {
        addAll(featuredProducts)
        addAll(newArrivals)
        addAll(sculptedRippleVase.similarProducts)
        add(
            Product(
                id = sculptedRippleVase.id,
                name = "The Sculpted Ripple Vase",
                price = sculptedRippleVase.price,
                imageUrl = sculptedRippleVase.images.first(),
                studio = sculptedRippleVase.artist.name,
                category = ceramicsCategory,
                isFavorited = sculptedRippleVase.isFavorited,
            )
        )
    }

    val allProducts: List<Product> = allProductsInternal.distinctBy(Product::id)

    fun findProductById(productId: String): Product? = allProducts.firstOrNull { it.id == productId }

    fun productDetailsFor(productId: String): ProductDetails {
        if (productId == sculptedRippleVase.id) {
            return sculptedRippleVase
        }

        val product = findProductById(productId) ?: return sculptedRippleVase
        return ProductDetails(
            id = product.id,
            name = product.name,
            collectionLabel = product.category.name,
            price = product.price,
            story = "A handcrafted piece selected from our atelier catalogue. Each item is made in limited batches and finished by hand.",
            material = "Hand-finished artisan material",
            dimensions = "Details available on request",
            images = listOf(product.imageUrl) + sculptedRippleVase.images.drop(1),
            artist = artist.copy(name = product.studio),
            reviews = reviews,
            similarProducts = allProducts
                .filter { it.id != product.id && it.category.id == product.category.id }
                .take(2),
            isFavorited = product.isFavorited,
        )
    }
}
