package com.example.myappmobile.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myappmobile.core.components.FavoriteButton
import com.example.myappmobile.core.components.SectionHeader
import com.example.myappmobile.core.theme.*
import com.example.myappmobile.data.MockData
import com.example.myappmobile.domain.Product

@Composable
fun FeaturedProductsSection(
    products: List<Product>,
    onProductClick: (String) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "CURATED SELECTION",
            style = MaterialTheme.typography.labelMedium,
            color = StoneGray,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(4.dp))
        SectionHeader(
            title = "Featured Artifacts",
            ctaText = "View all",
            onCtaClick = onViewAll,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            products.forEach { product ->
                FeaturedProductCard(
                    product = product,
                    onClick = { onProductClick(product.id) },
                    onFavoriteToggle = { onFavoriteToggle(product.id) },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }
}

@Composable
fun FeaturedProductCard(
    product: Product,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(MaterialTheme.shapes.large)
            .background(CreamDark)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.38f)),
                        startY = 140f,
                    ),
                ),
        )

        // Favorite button
        FavoriteButton(
            isFavorited = product.isFavorited,
            onToggle = onFavoriteToggle,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp),
        )

        // Studio + name + price
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    text = product.studio,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = White.copy(alpha = 0.70f),
                    ),
                )
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium.copy(color = White),
                )
            }
            Text(
                text = "$${product.price.toInt()}",
                style = MaterialTheme.typography.titleLarge.copy(color = White),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8)
@Composable
fun FeaturedProductsSectionPreview() {
    AtelierTheme {
        FeaturedProductsSection(
            products = MockData.featuredProducts,
            onProductClick = {},
            onFavoriteToggle = {},
            onViewAll = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8, name = "FeaturedProductCard")
@Composable
fun FeaturedProductCardPreview() {
    AtelierTheme {
        FeaturedProductCard(
            product = MockData.featuredProducts.first(),
            onClick = {},
            onFavoriteToggle = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}