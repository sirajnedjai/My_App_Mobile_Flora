package com.example.myappmobile.presentation.shop

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircleFilled
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Window
import androidx.compose.material.icons.outlined.Workspaces
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.myappmobile.core.components.FloraRemoteImage
import com.example.myappmobile.R
import com.example.myappmobile.core.components.BuyersOnlyNotice
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.FavoriteButton
import com.example.myappmobile.core.components.OutlineButton
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.components.ShimmerBox
import com.example.myappmobile.core.navigation.AppBottomBar
import com.example.myappmobile.core.navigation.Routes
import com.example.myappmobile.core.theme.Cream
import com.example.myappmobile.core.theme.CreamDark
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBeigeLight
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraCardBg
import com.example.myappmobile.core.theme.FloraDivider
import com.example.myappmobile.core.theme.FloraSelectedCard
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.FloraTheme
import com.example.myappmobile.core.theme.FloraWhite
import com.example.myappmobile.core.theme.SerifFontFamily
import com.example.myappmobile.core.theme.StoneFaint
import com.example.myappmobile.core.theme.StoneGray
import com.example.myappmobile.core.theme.Terracotta
import com.example.myappmobile.data.MockData
import com.example.myappmobile.domain.Product

@Composable
fun ShopScreen(
    onProductClick: (String) -> Unit = {},
    onBannerClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onRetry: () -> Unit = {},
    selectedRoute: String = Routes.SELLER,
    onBottomNavClick: (String) -> Unit = {},
    viewModel: ShopViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.favoriteMessage) {
        val message = uiState.favoriteMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearFavoriteMessage()
    }

    ShopScreenContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onCategorySelected = viewModel::onCategorySelected,
        onSortSelected = viewModel::onSortSelected,
        onProductClick = onProductClick,
        onFavoriteToggle = viewModel::onToggleFavorite,
        onBannerClick = onBannerClick,
        onCartClick = onCartClick,
        onFilterClick = onFilterClick,
        onRetry = onRetry,
        onLoadMore = viewModel::onLoadMore,
        onClearFilters = viewModel::clearFilters,
        selectedRoute = selectedRoute,
        onBottomNavClick = onBottomNavClick,
        snackbarHostState = snackbarHostState,
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
    onFilterClick: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onClearFilters: () -> Unit,
    selectedRoute: String,
    onBottomNavClick: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        containerColor = FloraBeige,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ShopHeader(
                title = uiState.title,
                subtitle = "A soft edit of handmade pieces, carefully arranged for modern living.",
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
        AnimatedContent(
            targetState = uiState.isLoading,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "shopLoadingTransition",
        ) { loading ->
            if (loading) {
                ShopLoadingState(modifier = Modifier.padding(padding))
            } else if (uiState.products.isEmpty() && uiState.error != null) {
                ShopErrorState(
                    message = uiState.error,
                    onRetry = onRetry,
                    modifier = Modifier.padding(padding),
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFF7F2EC),
                                    FloraBeige,
                                    Color(0xFFF1EBE4),
                                ),
                            ),
                        )
                        .padding(padding),
                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 36.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ShopHeroSection()
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ShopControlPanel(
                            query = uiState.searchQuery,
                            selectedCategoryId = uiState.selectedCategoryId,
                            categories = uiState.categories,
                            selectedSortId = uiState.selectedSortId,
                            sortOptions = uiState.sortOptions,
                            activeFiltersSummary = uiState.activeFiltersSummary,
                            onQueryChange = onSearchQueryChange,
                            onCategorySelected = onCategorySelected,
                            onSortSelected = onSortSelected,
                            onFilterClick = onFilterClick,
                            onClearFilters = onClearFilters,
                        )
                    }

                    if (!uiState.canUseWishlist) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            BuyersOnlyNotice(
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ProductSectionHeader(
                            visibleCount = uiState.visibleProducts.size,
                            totalCount = uiState.products.size,
                            hasFilters = uiState.activeFiltersSummary.isNotEmpty(),
                        )
                    }

                    if (uiState.error != null) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ShopInlineErrorCard(
                                message = uiState.error,
                                onRetry = onRetry,
                            )
                        }
                    }

                    if (uiState.visibleProducts.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyShopResults(
                                hasFilters = uiState.activeFiltersSummary.isNotEmpty() || uiState.searchQuery.isNotBlank(),
                            )
                        }
                    } else {
                        items(uiState.visibleProducts, key = Product::id) { product ->
                            LuxuryProductCard(
                                product = product,
                                rating = product.previewRating(),
                                canUseWishlist = uiState.canUseWishlist,
                                onClick = { onProductClick(product.id) },
                                onFavoriteToggle = { onFavoriteToggle(product.id) },
                                isFavoriteUpdating = product.id in uiState.pendingFavoriteIds,
                            )
                        }
                    }

                    if (uiState.canLoadMore) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier.padding(top = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                OutlineButton(
                                    text = stringResource(R.string.common_browse_more),
                                    onClick = onLoadMore,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Unable to load the shop",
            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = SerifFontFamily),
            color = FloraText,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = FloraTextSecondary,
        )
        Spacer(modifier = Modifier.height(20.dp))
        PrimaryButton(
            text = "Retry",
            onClick = onRetry,
        )
    }
}

@Composable
private fun ShopInlineErrorCard(
    message: String,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = FloraWhite.copy(alpha = 0.84f)),
        border = BorderStroke(1.dp, FloraDivider.copy(alpha = 0.8f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Some products could not be refreshed",
                    style = MaterialTheme.typography.titleSmall,
                    color = FloraText,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            OutlineButton(
                text = "Retry",
                onClick = onRetry,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShopHeader(
    title: String,
    subtitle: String,
    onFilterClick: () -> Unit,
    onCartClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "FLORA",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified,
                    ),
                    color = FloraTextSecondary,
                )
                Text(
                    text = "Curated handmade pieces",
                    style = MaterialTheme.typography.labelSmall,
                    color = FloraTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        actions = {
            PremiumIconAction(
                icon = Icons.Outlined.Tune,
                contentDescription = stringResource(R.string.common_filter),
                onClick = onFilterClick,
            )
            PremiumIconAction(
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
private fun PremiumIconAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = FloraSelectedCard.copy(alpha = 0.94f),
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, FloraDivider.copy(alpha = 0.75f)),
        modifier = Modifier.padding(end = 8.dp),
    ) {
        CircularIconButton(
            icon = icon,
            contentDescription = contentDescription,
            onClick = onClick,
            modifier = Modifier.size(44.dp),
        )
    }
}

@Composable
private fun ShopHeroSection() {
    FloraPromoBanner()
}

private const val FLORA_PROMO_ASSET_PATH = "videos/flora_promo.mp4"
private const val FLORA_PROMO_POSTER_ASSET_PATH = "images/flora_promo_poster.jpg"

@Composable
private fun FloraPromoBanner(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val assetExists = remember { floraPromoAssetExists(context) }
    val posterExists = remember { floraPromoPosterExists(context) }
    val posterModel = remember(posterExists) {
        if (posterExists) "file:///android_asset/$FLORA_PROMO_POSTER_ASSET_PATH" else null
    }

    if (!assetExists) {
        FloraPromoFallbackCard(
            modifier = modifier,
            posterModel = posterModel,
            showPlayOverlay = true,
        )
        return
    }

    val videoUri = remember {
        Uri.parse("asset:///$FLORA_PROMO_ASSET_PATH")
    }
    var isMuted by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var hasVideoError by remember { mutableStateOf(false) }
    var isVideoReady by remember { mutableStateOf(false) }

    val exoPlayer = remember(videoUri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(exoPlayer, isMuted, isPlaying) {
        exoPlayer.volume = if (isMuted) 0f else 1f
        exoPlayer.playWhenReady = isPlaying
        onDispose {
            exoPlayer.release()
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isVideoReady = playbackState == Player.STATE_READY
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                hasVideoError = true
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    if (hasVideoError) {
        FloraPromoFallbackCard(
            modifier = modifier,
            posterModel = posterModel,
            showPlayOverlay = true,
        )
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Cream.copy(alpha = 0.98f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(CreamDark),
            ) {
                AndroidView(
                    factory = { viewContext ->
                        PlayerView(viewContext).apply {
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            player = exoPlayer
                            setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { it.player = exoPlayer },
                )

                if (!isVideoReady) {
                    FloraPromoFallbackCardContent(
                        posterModel = posterModel,
                        isOverlay = true,
                        showPlayOverlay = false,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color(0x990F0B09),
                                ),
                            ),
                        ),
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = FloraSelectedCard.copy(alpha = 0.20f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.GraphicEq,
                            contentDescription = null,
                            tint = FloraWhite,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "FLORA PROMO",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = FloraWhite,
                        )
                    }
                }

                if (isVideoReady) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PromoIconControl(
                            icon = if (isPlaying) Icons.Outlined.PauseCircle else Icons.Outlined.PlayCircleFilled,
                            contentDescription = if (isPlaying) "Pause promo video" else "Play promo video",
                            onClick = { isPlaying = !isPlaying },
                        )
                        PromoIconControl(
                            icon = if (isMuted) Icons.Outlined.VolumeOff else Icons.Outlined.VolumeUp,
                            contentDescription = if (isMuted) "Unmute promo video" else "Mute promo video",
                            onClick = { isMuted = !isMuted },
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "From home to the world",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = SerifFontFamily,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = FloraText,
                )
                Text(
                    text = "Support women creators and discover unique handmade products through the FLORA edit.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FloraTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun PromoIconControl(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = Color(0xD7F8F1EA),
        shadowElevation = 8.dp,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = FloraText,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun FloraPromoFallbackCard(
    modifier: Modifier = Modifier,
    posterModel: String? = null,
    showPlayOverlay: Boolean,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Cream.copy(alpha = 0.98f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        FloraPromoFallbackCardContent(
            posterModel = posterModel,
            showPlayOverlay = showPlayOverlay,
        )
    }
}

@Composable
private fun FloraPromoFallbackCardContent(
    posterModel: String? = null,
    isOverlay: Boolean = false,
    showPlayOverlay: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (isOverlay) 0.dp else 220.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF7F0EA),
                        Color(0xFFF1E5D9),
                    ),
                ),
            ),
    ) {
        if (posterModel != null) {
            FloraRemoteImage(
                imageUrl = posterModel,
                contentDescription = "FLORA promo poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x26FFFFFF),
                            Color(0x661A120E),
                            Color(0xC916110E),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isOverlay) FloraSelectedCard.copy(alpha = 0.20f) else Terracotta.copy(alpha = 0.10f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = if (isOverlay) FloraWhite else Terracotta,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "FLORA PROMO",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isOverlay) FloraWhite else Terracotta,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (showPlayOverlay) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xD9F8F1EA),
                        shadowElevation = 10.dp,
                        modifier = Modifier.size(58.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.PlayCircleFilled,
                                contentDescription = null,
                                tint = FloraText,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }
                }
                Text(
                    text = "From home to the world",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = SerifFontFamily,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = FloraWhite,
                )
                Text(
                    text = "Support women creators and discover unique handmade products",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FloraWhite.copy(alpha = 0.90f),
                )
            }
        }
    }
}

private fun floraPromoAssetExists(context: Context): Boolean = runCatching {
    context.assets.open(FLORA_PROMO_ASSET_PATH).use { stream ->
        stream.available() >= 0
    }
}.getOrDefault(false)

private fun floraPromoPosterExists(context: Context): Boolean = runCatching {
    context.assets.open(FLORA_PROMO_POSTER_ASSET_PATH).use { stream ->
        stream.available() >= 0
    }
}.getOrDefault(false)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShopControlPanel(
    query: String,
    selectedCategoryId: String,
    categories: List<ShopCategoryUi>,
    selectedSortId: String,
    sortOptions: List<ShopSortUi>,
    activeFiltersSummary: List<String>,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onSortSelected: (String) -> Unit,
    onFilterClick: () -> Unit,
    onClearFilters: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Cream.copy(alpha = 0.98f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SearchAndFilterRow(
                query = query,
                onQueryChange = onQueryChange,
                onFilterClick = onFilterClick,
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionEyebrow(
                    title = stringResource(R.string.shop_curated_categories),
                    subtitle = stringResource(R.string.shop_curated_categories_subtitle),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    categories.forEach { category ->
                        val selected = category.id == selectedCategoryId
                        SelectionChip(
                            text = localizedShopCategoryLabel(category),
                            selected = selected,
                            leadingIcon = category.icon(),
                            onClick = { onCategorySelected(category.id) },
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionEyebrow(
                    title = "Sort The Edit",
                    subtitle = "Switch the order of the collection without leaving the page.",
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    sortOptions.forEach { option ->
                        SelectionChip(
                            text = localizedShopSortLabel(option),
                            selected = option.id == selectedSortId,
                            leadingIcon = Icons.Outlined.Window,
                            onClick = { onSortSelected(option.id) },
                            compact = true,
                        )
                    }
                }
            }

            AnimatedVisibility(visible = activeFiltersSummary.isNotEmpty()) {
                ActiveFiltersSummary(
                    summary = activeFiltersSummary,
                    onClearFilters = onClearFilters,
                )
            }
        }
    }
}

@Composable
private fun SearchAndFilterRow(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = stringResource(R.string.nav_search),
                    tint = FloraTextSecondary,
                )
            },
            placeholder = {
                Text(stringResource(R.string.shop_search_placeholder))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FloraBeigeLight,
                unfocusedContainerColor = FloraBeigeLight,
                focusedBorderColor = FloraBrown.copy(alpha = 0.55f),
                unfocusedBorderColor = FloraDivider,
                cursorColor = FloraBrown,
            ),
        )

        ElevatedActionPill(
            text = stringResource(R.string.common_filter),
            icon = Icons.Outlined.Tune,
            onClick = onFilterClick,
        )
    }
}

@Composable
private fun ElevatedActionPill(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(targetValue = 1f, label = "actionPillScale")

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        shadowElevation = 10.dp,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            FloraBrown,
                            Terracotta,
                        ),
                    ),
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FloraWhite,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = FloraWhite,
            )
        }
    }
}

@Composable
private fun SectionEyebrow(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = SerifFontFamily,
                fontStyle = FontStyle.Italic,
            ),
            color = FloraText,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = FloraTextSecondary,
        )
    }
}

@Composable
private fun SelectionChip(
    text: String,
    selected: Boolean,
    leadingIcon: ImageVector,
    onClick: () -> Unit,
    compact: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.985f,
        label = "selectionChipScale",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) FloraBrown else FloraSelectedCard,
        label = "selectionChipColor",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) FloraWhite else FloraText,
        label = "selectionChipText",
    )

    Surface(
        shape = RoundedCornerShape(if (compact) 18.dp else 22.dp),
        color = containerColor,
        shadowElevation = if (selected) 10.dp else 1.dp,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) FloraBrown else FloraDivider,
        ),
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = if (compact) 10.dp else 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (selected) FloraWhite else FloraTextSecondary,
                modifier = Modifier.size(15.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun FeaturedBannerSection(
    banner: ShopBannerUi,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(34.dp))
            .clickable(onClick = onClick),
    ) {
        FloraRemoteImage(
            imageUrl = banner.imageUrl,
            contentDescription = banner.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x472B1E16),
                            Color(0xB3201612),
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = FloraSelectedCard.copy(alpha = 0.20f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = FloraWhite,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(R.string.shop_seasonal_edit),
                        style = MaterialTheme.typography.labelLarge,
                        color = FloraWhite,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = banner.title,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = SerifFontFamily,
                    fontStyle = FontStyle.Italic,
                ),
                color = FloraWhite,
            )
            Text(
                text = banner.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = FloraWhite.copy(alpha = 0.90f),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = banner.ctaText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = FloraWhite,
                )
                Icon(
                    imageVector = Icons.Outlined.ArrowOutward,
                    contentDescription = null,
                    tint = FloraWhite,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun ProductSectionHeader(
    visibleCount: Int,
    totalCount: Int,
    hasFilters: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.shop_refined_discoveries),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = SerifFontFamily,
                    fontStyle = FontStyle.Italic,
                ),
                color = FloraText,
            )
            Text(
                text = if (hasFilters) {
                    "Filtered result: $visibleCount of $totalCount pieces in view"
                } else {
                    stringResource(R.string.shop_visible_count, visibleCount, totalCount)
                },
                style = MaterialTheme.typography.bodySmall,
                color = FloraTextSecondary,
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FloraSelectedCard,
            border = BorderStroke(1.dp, FloraDivider.copy(alpha = 0.75f)),
        ) {
            Text(
                text = "$visibleCount",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = FloraText,
            )
        }
    }
}

@Composable
private fun LuxuryProductCard(
    product: Product,
    rating: Float,
    canUseWishlist: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    isFavoriteUpdating: Boolean,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(targetValue = 1f, label = "productCardScale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Cream.copy(alpha = 0.98f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.82f)
                    .background(FloraCardBg),
            ) {
                FloraRemoteImage(
                    imageUrl = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)),
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color(0x360F0A08),
                                ),
                            ),
                        ),
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = FloraSelectedCard.copy(alpha = 0.90f),
                    border = BorderStroke(1.dp, FloraDivider.copy(alpha = 0.55f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = categoryIcon(product.category.id),
                            contentDescription = null,
                            tint = Terracotta,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = localizedShopCategoryName(product.category.id, product.category.name),
                            style = MaterialTheme.typography.labelMedium,
                            color = FloraText,
                        )
                    }
                }

                if (canUseWishlist) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        shape = CircleShape,
                        color = Cream.copy(alpha = 0.90f),
                        shadowElevation = 8.dp,
                    ) {
                        FavoriteButton(
                            isFavorited = product.isFavorited,
                            onToggle = onFavoriteToggle,
                            enabled = !isFavoriteUpdating,
                            modifier = Modifier.size(44.dp),
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xC71F1814),
                ) {
                    Text(
                        text = product.studio,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = FloraWhite,
                        maxLines = 1,
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = FloraText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = stringResource(
                        R.string.shop_hand_finished_piece,
                        localizedShopCategoryName(product.category.id, product.category.name).lowercase(),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Price",
                            style = MaterialTheme.typography.labelSmall,
                            color = StoneGray,
                        )
                        Text(
                            text = "$${"%.0f".format(product.price)}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = SerifFontFamily,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = FloraBrown,
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = StoneFaint,
                        border = BorderStroke(1.dp, FloraDivider.copy(alpha = 0.65f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = Terracotta,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "${"%.1f".format(rating)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = FloraTextSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopLoadingState(
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF7F2EC),
                        FloraBeige,
                    ),
                ),
            ),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ShimmerBox(height = 278.dp) }
        item { ShimmerBox(height = 260.dp) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ShimmerBox(height = 300.dp, modifier = Modifier.weight(1f))
                ShimmerBox(height = 300.dp, modifier = Modifier.weight(1f))
            }
        }
    }
}

private fun Product.previewRating(): Float {
    val seed = id.lastOrNull()?.code ?: name.lastOrNull()?.code ?: 0
    return 4.2f + (seed % 7) / 10f
}

@Composable
private fun localizedShopCategoryLabel(category: ShopCategoryUi): String = when (category.id) {
    ShopCategoryUi.ALL.id -> stringResource(R.string.shop_category_handmade)
    "jewelry" -> stringResource(R.string.category_jewelry)
    "home" -> stringResource(R.string.category_home)
    "textiles" -> stringResource(R.string.category_textiles)
    "ceramics" -> stringResource(R.string.category_ceramics)
    else -> category.title
}

@Composable
private fun localizedShopCategoryName(categoryId: String, fallback: String): String = when (categoryId) {
    "jewelry" -> stringResource(R.string.category_jewelry)
    "home" -> stringResource(R.string.category_home)
    "textiles" -> stringResource(R.string.category_textiles)
    "ceramics" -> stringResource(R.string.category_ceramics)
    else -> fallback
}

@Composable
private fun localizedShopSortLabel(option: ShopSortUi): String = when (option.id) {
    ShopSortUi.POPULAR.id -> stringResource(R.string.shop_sort_popular)
    ShopSortUi.NEWEST.id -> stringResource(R.string.shop_sort_newest)
    ShopSortUi.CURATED.id -> stringResource(R.string.shop_sort_curated)
    else -> option.title
}

private fun ShopCategoryUi.icon(): ImageVector = categoryIcon(id)

private fun categoryIcon(categoryId: String): ImageVector = when (categoryId) {
    "jewelry" -> Icons.Outlined.Checkroom
    "home" -> Icons.Outlined.Window
    "textiles" -> Icons.Outlined.Spa
    "ceramics" -> Icons.Outlined.LocalFlorist
    else -> Icons.Outlined.Workspaces
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveFiltersSummary(
    summary: List<String>,
    onClearFilters: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Active Filters",
                style = MaterialTheme.typography.titleMedium,
                color = FloraText,
            )
            Text(
                text = "Clear",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = Terracotta,
                modifier = Modifier.clickable(onClick = onClearFilters),
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            summary.forEach { label ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = FloraSelectedCard,
                    border = BorderStroke(1.dp, FloraDivider.copy(alpha = 0.75f)),
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = FloraText,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyShopResults(
    hasFilters: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = FloraCardBg,
            ) {
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = Terracotta,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
            Text(
                text = "No products found",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = SerifFontFamily,
                    fontStyle = FontStyle.Italic,
                ),
                color = FloraText,
            )
            Text(
                text = if (hasFilters) {
                    "Try adjusting your filters to discover more items."
                } else {
                    "The current collection is momentarily quiet. Return soon for more handmade pieces."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = FloraTextSecondary,
            )
        }
    }
}

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
                    ShopCategoryUi("textiles", "Textiles"),
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
                products = MockData.allProducts.take(6),
                visibleProducts = MockData.allProducts.take(6),
                activeFiltersSummary = listOf("Ceramics", "Premium"),
                isLoading = false,
            ),
            onSearchQueryChange = {},
            onCategorySelected = {},
            onSortSelected = {},
            onProductClick = {},
            onFavoriteToggle = {},
            onBannerClick = {},
            onCartClick = {},
            onFilterClick = {},
            onRetry = {},
            onLoadMore = {},
            onClearFilters = {},
            selectedRoute = Routes.SELLER,
            onBottomNavClick = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
