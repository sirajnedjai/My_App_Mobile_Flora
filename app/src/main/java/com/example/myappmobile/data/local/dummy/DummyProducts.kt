package com.example.myappmobile.data.local.dummy

import com.example.myappmobile.domain.model.Product
import com.example.myappmobile.domain.model.ProductVariant

object DummyProducts {

    val sculptedVase = Product(
        id = "p1",
        name = "The Sculpted Ripple Vase",
        price = 185.0,
        imageUrl = "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800",
        studio = "FLORA CERAMICS",
        storeId = "s1",
        category = "Ceramics",
        collectionLabel = "SPRING COLLECTION '24",
        material = "Toasted Stoneware",
        dimensions = "9\" H × 6\" W",
        stockCount = 12,
        story = "Each piece is hand-thrown in our coastal workshop, capturing the rhythmic movement of the tide. The unglazed exterior celebrates the raw beauty of toasted stoneware.",
        images = listOf(
            "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800",
            "https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=400",
            "https://images.unsplash.com/photo-1612198188060-c7c2a3b66eae?w=400",
        ),
        isFavorited = true,
        variants = listOf(
            ProductVariant("v1", palette = "Earth Umber", size = "Medium (45cm)", availability = "12 In Stock"),
        ),
    )

    val adobeVase = Product(
        id = "p2",
        name = "Adobe Textured Vase",
        price = 185.0,
        imageUrl = "https://images.unsplash.com/photo-1608501078713-8e445a709b39?w=600",
        studio = "CLAY & FORM",
        storeId = "s1",
        category = "Ceramics",
        description = "Limited Edition / Sandstone",
        stockCount = 5,
        isLimitedEdition = true,
    )

    val aureliaLink = Product(
        id = "p3",
        name = "Aurelia Link Necklace",
        price = 420.0,
        imageUrl = "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=600",
        studio = "AURUM CRAFT",
        storeId = "s2",
        category = "Jewelry",
        description = "Recycled 18k Gold",
        stockCount = 8,
    )

    val heirloomTote = Product(
        id = "p4",
        name = "Heirloom Day Bag",
        price = 420.0,
        imageUrl = "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=600",
        studio = "ALBA MURÁL",
        storeId = "s3",
        category = "Leather",
        stockCount = 3,
        isFavorited = true,
    )

    val botanicalSilk = Product(
        id = "p5",
        name = "Botanical Silk Square",
        price = 95.0,
        imageUrl = "https://images.unsplash.com/photo-1617952739429-b4f8a52ccad6?w=600",
        studio = "ELF-READ TEXTILE",
        storeId = "s4",
        category = "Textiles",
        isFavorited = true,
    )

    val pavilionChair = Product(
        id = "p6",
        name = "The Pavilion Chair",
        price = 890.0,
        imageUrl = "https://images.unsplash.com/photo-1567538096630-e0c55bd6374c?w=600",
        studio = "OAK & BRASS",
        storeId = "s5",
        category = "Furniture",
        isFavorited = true,
    )

    val sculpturalVase = Product(
        id = "p7",
        name = "The Sculptural Vase No. 12",
        price = 340.0,
        imageUrl = "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=600",
        studio = "FLORA CERAMICS",
        category = "Ceramics",
        description = "Hand-thrown stoneware with matte glaze finish.",
        isLimitedEdition = true,
        stockCount = 4,
    )

    val solsticePendant = Product(
        id = "p8",
        name = "Solstice Pearl Pendant",
        price = 285.0,
        imageUrl = "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=600",
        studio = "AURUM CRAFT",
        category = "Jewelry",
        description = "18k Gold & Keshi Pearl",
    )

    val earthboundSet = Product(
        id = "p9",
        name = "Earthbound Serving Set",
        price = 190.0,
        imageUrl = "https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=600",
        studio = "STONE TABLE",
        category = "Ceramics",
        description = "Set of 4 artisan plates.",
    )

    val lunarHoops = Product(
        id = "p10",
        name = "Lunar Silver Hoops",
        price = 210.0,
        imageUrl = "https://images.unsplash.com/photo-1630019852942-f89202989a59?w=600",
        studio = "FORGE & FORM",
        category = "Jewelry",
        description = "Hand-carved sterling silver.",
    )

    val wabiSabiBowl = Product(
        id = "p11",
        name = "Wabi-Sabi Cereal Bowl",
        price = 65.0,
        imageUrl = "https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=600",
        studio = "FLORA CERAMICS",
        category = "Ceramics",
        description = "Signature crackle glaze stoneware.",
    )

    val rawLinenThrow = Product(
        id = "p12",
        name = "Raw Linen Throw",
        price = 85.0,
        imageUrl = "https://images.unsplash.com/photo-1617952739429-b4f8a52ccad6?w=600",
        studio = "LOOM & WEFT",
        category = "Textiles",
        description = "Natural Beige",
    )

    val artisanStonewareVase = Product(
        id = "p13",
        name = "Artisan Stoneware Vase",
        price = 120.0,
        imageUrl = "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=600",
        studio = "FLORA CERAMICS",
        category = "Ceramics",
        description = "Earthy Grey / Small",
    )

    val vauLeatherTote = Product(
        id = "p14",
        name = "Veau Leather Tote",
        price = 340.0,
        imageUrl = "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=800",
        studio = "L'ARTISAN PARIS",
        category = "Leather",
        isFavorited = true,
    )

    // Seller inventory products
    val charcoalPitcher = Product(
        id = "sp1",
        name = "Charcoal Pitcher No. 14",
        price = 240.0,
        imageUrl = "https://images.unsplash.com/photo-1494790108755-2616b612b47c?w=400",
        studio = "FLORA CERAMICS",
        storeId = "s1",
        category = "Ceramics",
        description = "Hand-thrown stoneware, matte glaze",
        stockCount = 12,
    )

    val heritageLinen = Product(
        id = "sp2",
        name = "Heritage Linen Stack",
        price = 185.0,
        imageUrl = "https://images.unsplash.com/photo-1617952739429-b4f8a52ccad6?w=400",
        studio = "FLORA STUDIO",
        storeId = "s1",
        category = "Textiles",
        description = "Organic flax, double-weave texture",
        stockCount = 2,
    )

    val nocturneVessel = Product(
        id = "sp3",
        name = "Nocturne Scented Vessel",
        price = 95.0,
        imageUrl = "https://images.unsplash.com/photo-1608501078713-8e445a709b39?w=400",
        studio = "FLORA STUDIO",
        storeId = "s1",
        category = "Ceramics",
        description = "Hand-poured soy, amberwood & silk",
        stockCount = 28,
    )

    val earthenwareTaperVase = Product(
        id = "sp4",
        name = "Earthenware Taper Vase",
        price = 185.0,
        imageUrl = "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=400",
        studio = "FLORA CERAMICS",
        storeId = "s1",
        category = "Ceramics",
    )

    val rawIndigoThrow = Product(
        id = "sp5",
        name = "Raw Indigo Throw",
        price = 240.0,
        imageUrl = "https://images.unsplash.com/photo-1617952739429-b4f8a52ccad6?w=400",
        studio = "FLORA STUDIO",
        storeId = "s1",
        category = "Textiles",
    )

    val speckledServingBowl = Product(
        id = "sp6",
        name = "Speckled Serving Bowl",
        price = 95.0,
        imageUrl = "https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=400",
        studio = "FLORA CERAMICS",
        storeId = "s1",
        category = "Ceramics",
    )

    val tonalCeramicVessel = Product(
        id = "sp7",
        name = "Tonal Ceramic Vessel",
        price = 124.0,
        imageUrl = "https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=600",
        studio = "FLORA CERAMICS",
        category = "Ceramics",
        description = "Hand-thrown, Sand Finish",
        stockCount = 7,
    )

    val signatureCashmereThrow = Product(
        id = "sp8",
        name = "Signature Cashmere Throw",
        price = 385.0,
        imageUrl = "https://images.unsplash.com/photo-1617952739429-b4f8a52ccad6?w=600",
        studio = "LOOM & WEFT",
        category = "Textiles",
        description = "Charcoal, Ultra-fine Weave",
        stockCount = 2,
    )

    val allProducts = listOf(
        sculpturalVase, solsticePendant, earthboundSet,
        lunarHoops, wabiSabiBowl, adobeVase,
        aureliaLink, heirloomTote, botanicalSilk, pavilionChair,
        rawLinenThrow, artisanStonewareVase, vauLeatherTote,
    )

    val wishlistProducts = listOf(sculptedVase, heirloomTote, botanicalSilk, pavilionChair)

    val sellerProducts = listOf(charcoalPitcher, heritageLinen, nocturneVessel)

    val curatedWorks = listOf(earthenwareTaperVase, rawIndigoThrow, speckledServingBowl)
}
