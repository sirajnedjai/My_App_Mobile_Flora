package com.example.myappmobile.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myappmobile.core.components.AtelierDivider
import com.example.myappmobile.core.components.ShimmerBox
import com.example.myappmobile.core.navigation.AppBottomBar
import com.example.myappmobile.core.theme.*
import com.example.myappmobile.data.MockData
import com.example.myappmobile.domain.Product
import com.example.myappmobile.presentation.home.components.BannerSection
import com.example.myappmobile.presentation.home.components.CategoriesRow
import com.example.myappmobile.presentation.home.components.FeaturedProductsSection

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    selectedRoute: String = "home",
    onCategoryClick: (String) -> Unit = {},
    onProductClick: (String) -> Unit = {},
    onViewAllFeatured: () -> Unit = {},
    onEmailChange: (String) -> Unit = {},
    onSubscribe: () -> Unit = {},
    onFavoriteToggle: (String) -> Unit = {},
    onBottomNavClick: (String) -> Unit = {},
    onCartClick: () -> Unit = {},
) {
    Scaffold(
        topBar = { HomeTopBar(onCartClick = onCartClick) },
        containerColor = Cream,
        bottomBar = {
            AppBottomBar(
                selectedRoute = selectedRoute,
                onNavigate = onBottomNavClick,
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            HomeLoadingState(modifier = Modifier.padding(padding))
        } else {
            HomeContent(
                uiState = uiState,
                onCategoryClick = onCategoryClick,
                onProductClick = onProductClick,
                onViewAllFeatured = onViewAllFeatured,
                onEmailChange = onEmailChange,
                onSubscribe = onSubscribe,
                onFavoriteToggle = onFavoriteToggle,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(onCartClick: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
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

// ─── Main Scrollable Content ──────────────────────────────────────────────────

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onCategoryClick: (String) -> Unit,
    onProductClick: (String) -> Unit,
    onViewAllFeatured: () -> Unit,
    onEmailChange: (String) -> Unit,
    onSubscribe: () -> Unit,
    onFavoriteToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Cream),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        // Categories row
        item {
            CategoriesRow(
                categories = uiState.categories,
                onCategoryClick = onCategoryClick,
            )
        }

        // Hero banner
        item {
            uiState.banner?.let { banner ->
                BannerSection(
                    banner = banner,
                    onCtaClick = {},
                )
            }
            Spacer(Modifier.height(36.dp))
        }

        // Featured artifacts
        item {
            FeaturedProductsSection(
                products = uiState.featuredProducts,
                onProductClick = onProductClick,
                onFavoriteToggle = onFavoriteToggle,
                onViewAll = onViewAllFeatured,
            )
            Spacer(Modifier.height(40.dp))
        }

        // New Arrivals
        item {
            Column {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "FRESHLY HARVESTED",
                        style = MaterialTheme.typography.labelMedium,
                        color = StoneGray,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "New Arrivals",
                        style = MaterialTheme.typography.headlineMedium,
                        color = CharcoalDark,
                    )
                }
                Spacer(Modifier.height(16.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(uiState.newArrivals) { product ->
                        NewArrivalCard(
                            product = product,
                            onClick = { onProductClick(product.id) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(48.dp))
        }

        // Newsletter
        item {
            AtelierDivider(modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(32.dp))
            NewsletterSection(
                email = uiState.emailInput,
                isSubscribed = uiState.isSubscribed,
                onEmailChange = onEmailChange,
                onSubscribe = onSubscribe,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
    }
}

// ─── New Arrival Card ─────────────────────────────────────────────────────────

@Composable
private fun NewArrivalCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(200.dp)
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
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

// ─── Newsletter Section ───────────────────────────────────────────────────────

@Composable
private fun NewsletterSection(
    email: String,
    isSubscribed: Boolean,
    onEmailChange: (String) -> Unit,
    onSubscribe: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Join The Atelier",
            style = MaterialTheme.typography.headlineMedium,
            color = CharcoalDark,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Receive exclusive stories of our artisans\nand early access to limited collections.",
            style = MaterialTheme.typography.bodySmall,
            color = StoneGray,
        )
        Spacer(Modifier.height(20.dp))

        if (isSubscribed) {
            Text(
                text = "Thank you for joining ✦",
                style = MaterialTheme.typography.bodyMedium,
                color = Terracotta,
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp))
                    .border(0.5.dp, StoneLight, RoundedCornerShape(50.dp))
                    .background(White)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = CharcoalDark),
                    decorationBox = { innerTextField ->
                        if (email.isEmpty()) {
                            Text(
                                text = "Your email address",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StoneGray,
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.weight(1f),
                )
                Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Subscribe",
                    tint = Terracotta,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onSubscribe),
                )
            }
        }
    }
}

// ─── Loading State ────────────────────────────────────────────────────────────

@Composable
private fun HomeLoadingState(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Cream)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp),
    ) {
        item { ShimmerBox(height = 340.dp) }
        item { ShimmerBox(height = 24.dp, modifier = Modifier.fillMaxWidth(0.5f)) }
        items(3) { ShimmerBox(height = 280.dp) }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8, name = "Home — Loaded")
@Composable
fun HomeScreenPreview() {
    AtelierTheme {
        HomeScreen(
            uiState = HomeUiState(
                isLoading = false,
                banner = MockData.banner,
                categories = MockData.categories,
                featuredProducts = MockData.featuredProducts,
                newArrivals = MockData.newArrivals,
            ),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8, name = "Home — Loading")
@Composable
fun HomeScreenLoadingPreview() {
    AtelierTheme {
        HomeScreen(uiState = HomeUiState(isLoading = true))
    }
}
