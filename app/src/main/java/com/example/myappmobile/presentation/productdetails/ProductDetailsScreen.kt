package com.example.myappmobile.presentation.productdetails

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myappmobile.core.components.AtelierDivider
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.components.ShimmerBox
import com.example.myappmobile.core.components.StarRatingRow
import com.example.myappmobile.core.theme.*
import com.example.myappmobile.data.MockData
import com.example.myappmobile.domain.ArtistProfile
import com.example.myappmobile.domain.Product
import com.example.myappmobile.domain.ProductDetails
import com.example.myappmobile.domain.Review

@Composable
fun ProductDetailsScreen(
    uiState: ProductDetailsUiState,
    onBack: () -> Unit = {},
    onAddToCart: () -> Unit = {},
    onReservePickup: () -> Unit = {},
    onVisitStudio: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onSimilarProductClick: (String) -> Unit = {},
    onSelectImage: (Int) -> Unit = {},
) {
    Scaffold(
        topBar = { ProductDetailsTopBar(onBack = onBack, onCartClick = onCartClick) },
        containerColor = Cream,
    ) { padding ->
        if (uiState.isLoading) {
            ProductDetailsLoadingState(modifier = Modifier.padding(padding))
        } else {
            uiState.product?.let { product ->
                ProductDetailsContent(
                    product = product,
                    selectedImageIndex = uiState.selectedImageIndex,
                    addedToCart = uiState.addedToCart,
                    onAddToCart = onAddToCart,
                    onReservePickup = onReservePickup,
                    onVisitStudio = onVisitStudio,
                    onSimilarProductClick = onSimilarProductClick,
                    onSelectImage = onSelectImage,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailsTopBar(
    onBack: () -> Unit,
    onCartClick: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = CharcoalDark,
                )
            }
        },
        title = {
            Text(
                text = "The Atelier",
                style = MaterialTheme.typography.headlineMedium,
                color = CharcoalDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        actions = {
            IconButton(onClick = onCartClick) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingBag,
                    contentDescription = "Cart",
                    tint = CharcoalDark,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream),
    )
}

// ─── Main Content ─────────────────────────────────────────────────────────────

@Composable
private fun ProductDetailsContent(
    product: ProductDetails,
    selectedImageIndex: Int,
    addedToCart: Boolean,
    onAddToCart: () -> Unit,
    onReservePickup: () -> Unit,
    onVisitStudio: () -> Unit,
    onSimilarProductClick: (String) -> Unit,
    onSelectImage: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Cream),
    ) {
        // Image gallery
        item {
            ProductImageGallery(
                images = product.images,
                selectedIndex = selectedImageIndex,
                onSelect = onSelectImage,
                productName = product.name,
            )
        }

        // Collection + Name + Price
        item {
            Spacer(Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = product.collectionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Terracotta,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.displaySmall,
                    color = CharcoalDark,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "$%.2f".format(product.price),
                    style = MaterialTheme.typography.titleLarge,
                    color = CharcoalMid,
                )
            }
        }

        // Story
        item {
            Spacer(Modifier.height(20.dp))
            AtelierDivider(modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "THE STORY",
                    style = MaterialTheme.typography.labelSmall,
                    color = StoneGray,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = product.story,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalMid,
                )
            }
        }

        // Specs
        item {
            Spacer(Modifier.height(20.dp))
            ProductSpecsRow(
                material = product.material,
                dimensions = product.dimensions,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        // Artist
        item {
            Spacer(Modifier.height(20.dp))
            AtelierDivider(modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(16.dp))
            ArtistCard(
                artist = product.artist,
                onVisitStudio = onVisitStudio,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(24.dp))
        }

        // CTA Buttons
        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PrimaryButton(
                    text = if (addedToCart) "Added to Cart ✓" else "Add to Cart",
                    onClick = onAddToCart,
                    enabled = !addedToCart,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(
                    onClick = onReservePickup,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CharcoalDark),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Reserve for Pick-up",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            Spacer(Modifier.height(40.dp))
        }

        // Reviews header
        item {
            AtelierDivider(modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(24.dp))
            ReviewsHeader(
                averageRating = 4.9f,
                subtitle = "Reflections from those who own this piece.",
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(20.dp))
        }

        // Reviews list
        items(product.reviews) { review ->
            ReviewCard(
                review = review,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(4.dp))
        }

        // You May Also Admire
        item {
            Spacer(Modifier.height(24.dp))
            AtelierDivider(modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(28.dp))
            Text(
                text = "You May Also Admire",
                style = MaterialTheme.typography.headlineSmall,
                color = CharcoalDark,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(16.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(product.similarProducts) { p ->
                    SimilarProductCard(
                        product = p,
                        onClick = { onSimilarProductClick(p.id) },
                    )
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

// ─── Image Gallery ────────────────────────────────────────────────────────────

@Composable
private fun ProductImageGallery(
    images: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    productName: String,
) {
    Column {
        AnimatedContent(
            targetState = images.getOrElse(selectedIndex) { images.first() },
            transitionSpec = {
                fadeIn(animationSpec = tween(280)) togetherWith fadeOut(animationSpec = tween(280))
            },
            label = "mainImage",
        ) { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = productName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
            )
        }

        if (images.size > 1) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                images.forEachIndexed { index, url ->
                    val isSelected = index == selectedIndex
                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) Terracotta else Color.Transparent,
                        label = "thumbBorder",
                    )
                    AsyncImage(
                        model = url,
                        contentDescription = "Image ${index + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(MaterialTheme.shapes.small)
                            .border(1.5.dp, borderColor, MaterialTheme.shapes.small)
                            .background(CreamDark)
                            .clickable { onSelect(index) },
                    )
                }
            }
        }
    }
}

// ─── Specs Row ────────────────────────────────────────────────────────────────

@Composable
private fun ProductSpecsRow(
    material: String,
    dimensions: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(40.dp),
    ) {
        Column {
            Text(
                text = "MATERIAL",
                style = MaterialTheme.typography.labelSmall,
                color = StoneGray,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = material,
                style = MaterialTheme.typography.bodySmall,
                color = CharcoalMid,
            )
        }
        Column {
            Text(
                text = "DIMENSIONS",
                style = MaterialTheme.typography.labelSmall,
                color = StoneGray,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = dimensions,
                style = MaterialTheme.typography.bodySmall,
                color = CharcoalMid,
            )
        }
    }
}

// ─── Artist Card ──────────────────────────────────────────────────────────────

@Composable
private fun ArtistCard(
    artist: ArtistProfile,
    onVisitStudio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = White,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = artist.avatarUrl,
                contentDescription = artist.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CreamDark),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = CharcoalDark,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = StarGold,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "${artist.rating} (${artist.reviewCount})",
                        style = MaterialTheme.typography.bodySmall,
                        color = StoneGray,
                    )
                }
            }
            Text(
                text = artist.studioName,
                style = MaterialTheme.typography.labelMedium.copy(color = Terracotta),
                modifier = Modifier.clickable(onClick = onVisitStudio),
            )
        }
    }
}

// ─── Reviews Header ───────────────────────────────────────────────────────────

@Composable
private fun ReviewsHeader(
    averageRating: Float,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Collector Reviews",
                style = MaterialTheme.typography.headlineSmall,
                color = CharcoalDark,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = StoneGray,
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = averageRating.toString(),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 28.sp,
                    color = CharcoalDark,
                ),
            )
            StarRatingRow(rating = averageRating)
        }
    }
}

// ─── Review Card ──────────────────────────────────────────────────────────────

@Composable
private fun ReviewCard(
    review: Review,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row {
            repeat(review.rating) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = StarGold,
                    modifier = Modifier.size(13.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = review.text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = CharcoalMid,
                fontStyle = FontStyle.Italic,
            ),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = review.authorName,
            style = MaterialTheme.typography.labelSmall,
            color = StoneGray,
        )
        Spacer(Modifier.height(16.dp))
        AtelierDivider()
    }
}

// ─── Similar Product Card ─────────────────────────────────────────────────────

@Composable
private fun SimilarProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(160.dp)
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(MaterialTheme.shapes.large)
                .background(CreamDark),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = product.name,
            style = MaterialTheme.typography.titleMedium,
            color = CharcoalDark,
        )
        Text(
            text = "$${product.price.toInt()}",
            style = MaterialTheme.typography.bodySmall,
            color = StoneGray,
        )
    }
}

// ─── Loading ──────────────────────────────────────────────────────────────────

@Composable
private fun ProductDetailsLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cream)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ShimmerBox(height = 360.dp)
        ShimmerBox(height = 28.dp, modifier = Modifier.fillMaxWidth(0.7f))
        ShimmerBox(height = 18.dp, modifier = Modifier.fillMaxWidth(0.3f))
        ShimmerBox(height = 80.dp)
        ShimmerBox(height = 56.dp)
        ShimmerBox(height = 56.dp)
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8, name = "ProductDetails — Loaded")
@Composable
fun ProductDetailsScreenPreview() {
    AtelierTheme {
        ProductDetailsScreen(
            uiState = ProductDetailsUiState(
                isLoading = false,
                product = MockData.sculptedRippleVase,
                selectedImageIndex = 0,
            ),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8, name = "ProductDetails — Loading")
@Composable
fun ProductDetailsLoadingPreview() {
    AtelierTheme {
        ProductDetailsScreen(uiState = ProductDetailsUiState(isLoading = true))
    }
}

@Preview(showBackground = true, name = "ArtistCard")
@Composable
fun ArtistCardPreview() {
    AtelierTheme {
        ArtistCard(
            artist = MockData.artist,
            onVisitStudio = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "ReviewCard")
@Composable
fun ReviewCardPreview() {
    AtelierTheme {
        ReviewCard(
            review = MockData.reviews.first(),
            modifier = Modifier.padding(16.dp),
        )
    }
}
