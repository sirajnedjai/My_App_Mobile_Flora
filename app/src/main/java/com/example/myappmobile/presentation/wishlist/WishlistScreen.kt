package com.example.myappmobile.presentation.wishlist

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.BuyersOnlyNotice
import com.example.myappmobile.core.components.FavoriteButton
import com.example.myappmobile.core.components.OutlineButton
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.components.ShimmerBox
import com.example.myappmobile.core.navigation.AppBottomBar
import com.example.myappmobile.core.navigation.Routes
import com.example.myappmobile.R
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraDivider
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.FloraTheme
import com.example.myappmobile.core.theme.SerifFontFamily
import com.example.myappmobile.core.theme.StoneFaint
import com.example.myappmobile.core.theme.White
import com.example.myappmobile.data.MockData
import com.example.myappmobile.domain.Product

@Composable
fun WishlistScreen(
    onBack: () -> Unit = {},
    onProductClick: (String) -> Unit = {},
    onContinueShopping: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    selectedRoute: String = Routes.WISHLIST,
    onBottomNavClick: (String) -> Unit = {},
    viewModel: WishlistViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        val message = uiState.statusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearStatusMessage()
    }

    WishlistScreenContent(
        uiState = uiState,
        onBack = onBack,
        onProductClick = onProductClick,
        onRemove = viewModel::onRemoveFromWishlist,
        onAddToCart = viewModel::onAddToCart,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onContinueShopping = onContinueShopping,
        onCartClick = onCartClick,
        onSearchClick = onSearchClick,
        selectedRoute = selectedRoute,
        onBottomNavClick = onBottomNavClick,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun WishlistScreenContent(
    uiState: WishlistUiState,
    onBack: () -> Unit,
    onProductClick: (String) -> Unit,
    onRemove: (String) -> Unit,
    onAddToCart: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onContinueShopping: () -> Unit,
    onCartClick: () -> Unit,
    onSearchClick: () -> Unit,
    selectedRoute: String,
    onBottomNavClick: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        containerColor = FloraBeige,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            WishlistHeader(
                onBack = onBack,
                onSearchClick = onSearchClick,
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
        when {
            uiState.isLoading -> WishlistLoadingState(modifier = Modifier.padding(padding))
            !uiState.isBuyer -> SellerWishlistRestriction(
                message = uiState.restrictionMessage.orEmpty(),
                onContinueShopping = onContinueShopping,
                modifier = Modifier.padding(padding),
            )
            uiState.isEmpty -> EmptyWishlistState(
                title = if (uiState.query.isBlank()) {
                    stringResource(R.string.wishlist_empty_title)
                } else {
                    "No items match your search"
                },
                subtitle = if (uiState.query.isBlank()) {
                    stringResource(R.string.wishlist_empty_subtitle)
                } else {
                    "Try a different name, studio, or category inside your wishlist."
                },
                statusMessage = uiState.statusMessage,
                onContinueShopping = onContinueShopping,
                modifier = Modifier.padding(padding),
            )
            else -> WishlistList(
                query = uiState.query,
                products = uiState.products,
                statusMessage = uiState.statusMessage,
                onSearchQueryChanged = onSearchQueryChanged,
                onProductClick = onProductClick,
                onRemove = onRemove,
                onAddToCart = onAddToCart,
                pendingProductIds = uiState.pendingProductIds,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistHeader(
    onBack: () -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(R.string.wishlist_title),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = SerifFontFamily,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = FloraText,
                )
                Text(
                    text = stringResource(R.string.wishlist_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
            }
        },
        navigationIcon = {
            CircularIconButton(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.common_back),
                onClick = onBack,
            )
        },
        actions = {
            CircularIconButton(
                icon = Icons.Outlined.Search,
                contentDescription = stringResource(R.string.nav_search),
                onClick = onSearchClick,
            )
            CircularIconButton(
                icon = Icons.Outlined.ShoppingBag,
                contentDescription = stringResource(R.string.common_cart),
                onClick = onCartClick,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = FloraBeige,
            scrolledContainerColor = FloraBeige,
        ),
    )
}

@Composable
fun WishlistList(
    query: String,
    products: List<Product>,
    statusMessage: String?,
    onSearchQueryChanged: (String) -> Unit,
    onProductClick: (String) -> Unit,
    onRemove: (String) -> Unit,
    onAddToCart: (String) -> Unit,
    pendingProductIds: Set<String>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FloraBeige),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null)
                },
                placeholder = { Text("Search your wishlist") },
                shape = RoundedCornerShape(22.dp),
            )
        }
        if (!statusMessage.isNullOrBlank()) {
            item {
                WishlistStatusCard(message = statusMessage)
            }
        }
        items(products, key = Product::id) { product ->
            WishlistItemCard(
                product = product,
                rating = product.previewRating(),
                onClick = { onProductClick(product.id) },
                onRemove = { onRemove(product.id) },
                onAddToCart = { onAddToCart(product.id) },
                isFavoriteUpdating = product.id in pendingProductIds,
            )
        }
    }
}

@Composable
private fun SellerWishlistRestriction(
    message: String,
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FloraBeige)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BuyersOnlyNotice(message = message.ifBlank { "This feature is available for buyers only." })
        Spacer(modifier = Modifier.height(20.dp))
        PrimaryButton(
            text = stringResource(R.string.common_continue_shopping),
            onClick = onContinueShopping,
        )
    }
}

@Composable
fun WishlistItemCard(
    product: Product,
    rating: Float,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onAddToCart: () -> Unit,
    isFavoriteUpdating: Boolean,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.82f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                FavoriteButton(
                    isFavorited = product.isFavorited,
                    onToggle = onRemove,
                    enabled = !isFavoriteUpdating,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp),
                )
            }

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = product.studio,
                    style = MaterialTheme.typography.labelMedium,
                    color = FloraBrown,
                )
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = FloraText,
                )
                Text(
                    text = product.category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FloraTextSecondary,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "$${"%.0f".format(product.price)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = FloraText,
                    )
                    Text(
                        text = "★ ${"%.1f".format(rating)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = FloraTextSecondary,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlineButton(
                        text = stringResource(R.string.common_remove),
                        onClick = onRemove,
                        modifier = Modifier.weight(1f),
                        fillMaxWidth = false,
                        enabled = !isFavoriteUpdating,
                    )
                    PrimaryButton(
                        text = stringResource(R.string.common_add_to_cart),
                        onClick = onAddToCart,
                        modifier = Modifier.weight(1f),
                        fillMaxWidth = false,
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyWishlistState(
    title: String = "",
    subtitle: String = "",
    statusMessage: String? = null,
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FloraBeige)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = White.copy(alpha = 0.7f),
            shadowElevation = 8.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = FloraBrown,
                    modifier = Modifier.size(44.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title.ifBlank { stringResource(R.string.wishlist_empty_title) },
            style = MaterialTheme.typography.headlineMedium,
            color = FloraText,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = subtitle.ifBlank { stringResource(R.string.wishlist_empty_subtitle) },
            style = MaterialTheme.typography.bodyMedium,
            color = FloraTextSecondary,
            textAlign = TextAlign.Center,
        )
        if (!statusMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = FloraTextSecondary,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryButton(
            text = stringResource(R.string.common_continue_shopping),
            onClick = onContinueShopping,
        )
    }
}

@Composable
private fun WishlistStatusCard(message: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.7f)),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = FloraTextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        )
    }
}

@Composable
private fun WishlistLoadingState(
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FloraBeige),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(3) {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.72f)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ShimmerBox(height = 220.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(18.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StoneFaint),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(FloraDivider),
                    )
                }
            }
        }
    }
}

private fun Product.previewRating(): Float {
    val seed = id.lastOrNull()?.code ?: name.lastOrNull()?.code ?: 0
    return 4.2f + (seed % 7) / 10f
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1ED)
@Composable
private fun WishlistScreenPreview() {
    FloraTheme {
        WishlistScreenContent(
            uiState = WishlistUiState(
                products = MockData.allProducts.take(2).map { it.copy(isFavorited = true) },
                query = "",
                isLoading = false,
            ),
            onSearchQueryChanged = {},
            onBack = {},
            onProductClick = {},
            onRemove = {},
            onAddToCart = {},
            onContinueShopping = {},
            onCartClick = {},
            onSearchClick = {},
            selectedRoute = Routes.WISHLIST,
            onBottomNavClick = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1ED)
@Composable
private fun EmptyWishlistPreview() {
    FloraTheme {
        EmptyWishlistState(onContinueShopping = {})
    }
}
