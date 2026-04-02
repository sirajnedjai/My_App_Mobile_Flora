package com.example.myappmobile.presentation.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myappmobile.core.components.FavoriteButton
import com.example.myappmobile.core.components.ShimmerBox
import com.example.myappmobile.core.navigation.AppBottomBar
import com.example.myappmobile.core.navigation.Routes
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.FloraTheme
import com.example.myappmobile.core.theme.SerifFontFamily
import com.example.myappmobile.core.theme.StoneFaint
import com.example.myappmobile.core.theme.StoneGray
import com.example.myappmobile.core.theme.White
import com.example.myappmobile.data.MockData
import com.example.myappmobile.domain.Product

@Composable
fun ShopScreen(
    onProductClick: (String) -> Unit = {},
    onBannerClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    selectedRoute: String = Routes.SELLER,
    onBottomNavClick: (String) -> Unit = {},
    viewModel: ShopViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    ShopScreenContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onCategorySelected = viewModel::onCategorySelected,
        onSortSelected = viewModel::onSortSelected,
        onProductClick = onProductClick,
        onFavoriteToggle = viewModel::onToggleFavorite,
        onBannerClick = onBannerClick,
        onCartClick = onCartClick,
        onSearchClick = onSearchClick,
        onFilterClick = onFilterClick,
        selectedRoute = selectedRoute,
        onBottomNavClick = onBottomNavClick,
    )
}

@Composable
private fun ShopScreenContent(
    uiState: ShopUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onSortSelected: (String) -> Unit,
    onProductClick: (String) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onBannerClick: () -> Unit,
    onCartClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    selectedRoute: String,
    onBottomNavClick: (String) -> Unit,
) {
    Scaffold(
        containerColor = FloraBeige,
        topBar = {
            ShopHeader(
                title = uiState.title,
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearchClick = onSearchClick,
                onFilterClick = onFilterClick,
                onCartClick = onCartClick,
            )
        },
        bottomBar = {
            AppBottomBar(
                selectedRoute = selectedRoute,
                onNavigate = onBottomNavClick,
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            ShopLoadingState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FloraBeige)
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    uiState.banner?.let { banner ->
                        FeaturedBannerSection(
                            banner = banner,
                            onClick = onBannerClick,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }

                item {
                    CategoryRow(
                        categories = uiState.categories,
                        selectedCategoryId = uiState.selectedCategoryId,
                        onCategorySelected = onCategorySelected,
                    )
                }

                item {
                    FilterBar(
                        options = uiState.sortOptions,
                        selectedSortId = uiState.selectedSortId,
                        onSortSelected = onSortSelected,
                    )
                }

                item {
                    ProductGridSection(
                        products = uiState.products,
                        onProductClick = onProductClick,
                        onFavoriteToggle = onFavoriteToggle,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopHeader(
    title: String,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    onCartClick: () -> Unit,
) {
    Column(
        modifier = Modifier.background(FloraBeige),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = SerifFontFamily,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = FloraText,
                )
            },
            actions = {
                IconButton(onClick = onFilterClick) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Filter",
                        tint = FloraText,
                    )
                }
                IconButton(onClick = onCartClick) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingBag,
                        contentDescription = "Cart",
                        tint = FloraText,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = FloraBeige,
                scrolledContainerColor = FloraBeige,
            ),
        )

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(22.dp),
            leadingIcon = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                    )
                }
            },
            placeholder = { Text("Search crafted pieces") },
        )
    }
}

@Composable
fun FeaturedBannerSection(
    banner: ShopBannerUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = banner.imageUrl,
            contentDescription = banner.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.06f),
                            Color.Black.copy(alpha = 0.52f),
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = banner.title,
                style = MaterialTheme.typography.headlineMedium,
                color = White,
            )
            Text(
                text = banner.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = White.copy(alpha = 0.88f),
            )
            Text(
                text = banner.ctaText,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = White,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

@Composable
fun CategoryRow(
    categories: List<ShopCategoryUi>,
    selectedCategoryId: String,
    onCategorySelected: (String) -> Unit,
) {
    Column {
        Text(
            text = "Curated Categories",
            style = MaterialTheme.typography.titleLarge,
            color = FloraText,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(categories) { category ->
                val selected = category.id == selectedCategoryId
                AssistChip(
                    onClick = { onCategorySelected(category.id) },
                    label = { Text(category.title) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (selected) FloraBrown else White.copy(alpha = 0.7f),
                        labelColor = if (selected) White else FloraText,
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = if (selected) FloraBrown else StoneFaint,
                    ),
                )
            }
        }
    }
}

@Composable
fun FilterBar(
    options: List<ShopSortUi>,
    selectedSortId: String,
    onSortSelected: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(options) { option ->
            val selected = option.id == selectedSortId
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (selected) StoneFaint else Color.Transparent,
                modifier = Modifier.clickable { onSortSelected(option.id) },
            ) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) FloraText else StoneGray,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
fun ProductGridSection(
    products: List<Product>,
    onProductClick: (String) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Refined Discoveries",
            style = MaterialTheme.typography.titleLarge,
            color = FloraText,
        )
        Spacer(modifier = Modifier.height(14.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .height((((products.size + 1) / 2) * 280).dp),
        ) {
            items(products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    rating = product.previewRating(),
                    onClick = { onProductClick(product.id) },
                    onFavoriteToggle = { onFavoriteToggle(product.id) },
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    rating: Float,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                FavoriteButton(
                    isFavorited = product.isFavorited,
                    onToggle = onFavoriteToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                )
            }
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = FloraText,
                    maxLines = 2,
                )
                Text(
                    text = product.category.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "$${"%.0f".format(product.price)}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = FloraBrown,
                    )
                    Text(
                        text = "★ ${"%.1f".format(rating)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = FloraTextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ShopLoadingState(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FloraBeige),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ShimmerBox(height = 220.dp) }
        item { ShimmerBox(height = 48.dp) }
        item { ShimmerBox(height = 32.dp) }
        item { ShimmerBox(height = 560.dp) }
    }
}

private fun Product.previewRating(): Float = 4.2f + (id.last().code % 7) / 10f

@Preview(showBackground = true, backgroundColor = 0xFFF5F1ED)
@Composable
private fun ShopScreenPreview() {
    FloraTheme {
        ShopScreenContent(
            uiState = ShopUiState(
                categories = listOf(
                    ShopCategoryUi.ALL,
                    ShopCategoryUi("jewelry", "Accessories"),
                    ShopCategoryUi("home", "Decor"),
                ),
                sortOptions = listOf(
                    ShopSortUi.POPULAR,
                    ShopSortUi.NEWEST,
                    ShopSortUi.CURATED,
                ),
                banner = ShopBannerUi(
                    title = "FLORA Spring Atelier",
                    subtitle = "A curated selection of handmade pieces, quietly luxurious and made to live beautifully.",
                    ctaText = "Explore Collection",
                    imageUrl = MockData.banner.imageUrl,
                ),
                products = MockData.allProducts.take(4),
                isLoading = false,
            ),
            onSearchQueryChange = {},
            onCategorySelected = {},
            onSortSelected = {},
            onProductClick = {},
            onFavoriteToggle = {},
            onBannerClick = {},
            onCartClick = {},
            onSearchClick = {},
            onFilterClick = {},
            selectedRoute = Routes.SELLER,
            onBottomNavClick = {},
        )
    }
}
