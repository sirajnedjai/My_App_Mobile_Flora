package com.example.myappmobile.core.catalog

import com.example.myappmobile.domain.Product

data class FloraSubcategory(
    val id: String,
    val title: String,
    val aliases: List<String> = emptyList(),
)

data class FloraCategoryGroup(
    val id: String,
    val title: String,
    val subcategories: List<FloraSubcategory>,
)

object FloraCatalog {
    val categoryGroups = listOf(
        FloraCategoryGroup(
            id = "ceramics",
            title = "Ceramic",
            subcategories = listOf(
                FloraSubcategory("dishes", "Dishes", listOf("plate", "bowl", "serving", "set")),
                FloraSubcategory("cups", "Cups", listOf("cup", "mug", "tea", "pitcher")),
                FloraSubcategory("vases", "Vases", listOf("vase", "vessel")),
            ),
        ),
        FloraCategoryGroup(
            id = "textiles",
            title = "Textiles",
            subcategories = listOf(
                FloraSubcategory("blankets", "Blankets", listOf("throw", "blanket", "cashmere")),
                FloraSubcategory("pillows", "Pillows", listOf("pillow", "cushion")),
                FloraSubcategory("rugs", "Rugs", listOf("rug", "woven", "kilim")),
            ),
        ),
        FloraCategoryGroup(
            id = "jewelry",
            title = "Jewelry",
            subcategories = listOf(
                FloraSubcategory("rings", "Rings", listOf("ring")),
                FloraSubcategory("necklaces", "Necklaces", listOf("necklace", "pendant")),
                FloraSubcategory("earrings", "Earrings", listOf("earring", "hoop")),
            ),
        ),
        FloraCategoryGroup(
            id = "home",
            title = "Home",
            subcategories = listOf(
                FloraSubcategory("decor", "Decor", listOf("decor", "vase", "tray", "chair")),
                FloraSubcategory("storage", "Storage", listOf("storage", "basket", "tote", "bag")),
                FloraSubcategory("lighting", "Lighting", listOf("lamp", "lighting", "candle")),
            ),
        ),
    )

    val quickFilterTypes = listOf("All", "Limited Edition", "New Arrival", "Under $100", "Premium")

    fun groupFor(categoryId: String): FloraCategoryGroup? = categoryGroups.firstOrNull { it.id == categoryId }

    fun subcategoryLabel(categoryId: String, subcategoryId: String): String =
        groupFor(categoryId)?.subcategories?.firstOrNull { it.id == subcategoryId }?.title.orEmpty()

    fun categoryLabel(categoryId: String): String =
        groupFor(categoryId)?.title.orEmpty()

    fun matchesCategory(product: Product, categoryId: String): Boolean {
        if (categoryId.isBlank() || categoryId == "all") return true
        val category = product.category.name.lowercase()
        return when (categoryId) {
            "ceramics" -> category.contains("ceramic") || category.contains("decor")
            "textiles" -> category.contains("textile") || category.contains("handmade")
            "jewelry" -> category.contains("jewelry")
            "home" -> category.contains("home") || category.contains("decor") || category.contains("furniture")
            else -> category == categoryId
        }
    }

    fun matchesSubcategory(product: Product, categoryId: String, subcategoryId: String): Boolean {
        if (subcategoryId.isBlank()) return true
        val subcategory = groupFor(categoryId)?.subcategories?.firstOrNull { it.id == subcategoryId } ?: return true
        val haystack = buildString {
            append(product.name.lowercase())
            append(' ')
            append(product.studio.lowercase())
            append(' ')
            append(product.category.name.lowercase())
            append(' ')
            append(product.tags.joinToString(" ").lowercase())
        }
        return subcategory.aliases.any(haystack::contains) || haystack.contains(subcategory.title.lowercase())
    }

    fun matchesType(product: Product, type: String): Boolean {
        return when (type) {
            "", "All" -> true
            "Limited Edition" -> product.tags.any { it.equals("Limited", ignoreCase = true) }
            "New Arrival" -> product.tags.any { it.equals("New Arrival", ignoreCase = true) } ||
                product.name.contains("new", ignoreCase = true)
            "Under $100" -> product.price <= 100.0
            "Premium" -> product.price >= 180.0
            else -> true
        }
    }
}
