package com.example.myappmobile.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myappmobile.core.components.FloraRemoteImage
import com.example.myappmobile.core.components.AtelierDivider
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.components.SellerApprovalBadge
import com.example.myappmobile.core.components.ShimmerBox
import com.example.myappmobile.core.catalog.FloraCatalog
import com.example.myappmobile.core.navigation.AppBottomBar
import com.example.myappmobile.R
import com.example.myappmobile.core.theme.*
import com.example.myappmobile.data.MockData
import com.example.myappmobile.domain.Category
import com.example.myappmobile.domain.Product
import com.example.myappmobile.domain.model.User
import com.example.myappmobile.presentation.home.components.BannerSection
import com.example.myappmobile.presentation.home.components.CategoriesRow
import com.example.myappmobile.presentation.home.components.FeaturedProductsSection
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    selectedRoute: String = "home",
    onCategoryClick: (String) -> Unit = {},
    onCategorySelection: (String, String) -> Unit = { _, _ -> },
    onProductClick: (String) -> Unit = {},
    onViewAllFeatured: () -> Unit = {},
    onEmailChange: (String) -> Unit = {},
    onSubscribe: () -> Unit = {},
    onFavoriteToggle: (String) -> Unit = {},
    onFavoriteMessageShown: () -> Unit = {},
    onRetry: () -> Unit = {},
    onBottomNavClick: (String) -> Unit = {},
    onCartClick: () -> Unit = {},
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.favoriteMessage) {
        val message = uiState.favoriteMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onFavoriteMessageShown()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            HomeCategoryDrawer(
                categories = uiState.categories,
                onCategorySelection = { categoryId, subcategoryId ->
                    onCategorySelection(categoryId, subcategoryId)
                    scope.launch { drawerState.close() }
                },
            )
        },
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                HomeTopBar(
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    onCartClick = onCartClick,
                )
            },
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
            } else if (!uiState.hasContent && uiState.error != null) {
                HomeErrorState(
                    message = uiState.error,
                    onRetry = onRetry,
                    modifier = Modifier.padding(padding),
                )
            } else {
                HomeContent(
                    uiState = uiState,
                    onCategoryClick = onCategoryClick,
                    onProductClick = onProductClick,
                    onViewAllFeatured = onViewAllFeatured,
                    onEmailChange = onEmailChange,
                    onSubscribe = onSubscribe,
                    onFavoriteToggle = onFavoriteToggle,
                    onRetry = onRetry,
                    pendingFavoriteIds = uiState.pendingFavoriteIds,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            TopBarIconSurface {
                CircularIconButton(
                    icon = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.common_menu),
                    onClick = onMenuClick,
                    modifier = Modifier.size(42.dp),
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.common_the_atelier),
                style = MaterialTheme.typography.headlineMedium,
                color = CharcoalDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        actions = {
            TopBarIconSurface {
                CircularIconButton(
                    icon = Icons.Outlined.ShoppingBag,
                    contentDescription = stringResource(R.string.common_cart),
                    onClick = onCartClick,
                    modifier = Modifier.size(42.dp),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream),
    )
}

@Composable
private fun HomeCategoryDrawer(
    categories: List<Category>,
    onCategorySelection: (String, String) -> Unit,
) {
    var expandedCategoryId by rememberSaveable { mutableStateOf(FloraCatalog.categoryGroups.firstOrNull()?.id) }
    var selectedCategoryId by rememberSaveable { mutableStateOf("") }
    var selectedSubcategoryId by rememberSaveable { mutableStateOf("") }
    ModalDrawerSheet(
        modifier = Modifier.width(312.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "FLORA",
                    style = MaterialTheme.typography.labelLarge,
                    color = Terracotta,
                )
                Text(
                    text = stringResource(R.string.home_browse_categories),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.home_browse_categories_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = StoneGray,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FloraCatalog.categoryGroups.forEach { group ->
                    val category = categories.firstOrNull { it.id == group.id }
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = group.title,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        },
                        selected = selectedCategoryId == group.id,
                        onClick = {
                            expandedCategoryId = if (expandedCategoryId == group.id) null else group.id
                            selectedCategoryId = group.id
                        },
                        icon = {
                            val painter = category?.let { runCatching { painterResource(it.iconRes) }.getOrNull() }
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Terracotta.copy(alpha = 0.12f),
                            ) {
                                if (painter != null) {
                                    Icon(
                                        painter = painter,
                                        contentDescription = group.title,
                                        tint = Terracotta,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .size(18.dp),
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = group.title,
                                        tint = Terracotta,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .size(18.dp),
                                    )
                                }
                            }
                        },
                        badge = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                                contentDescription = null,
                                tint = StoneGray,
                                modifier = Modifier.size(14.dp),
                            )
                        },
                        shape = RoundedCornerShape(22.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = MaterialTheme.colorScheme.surface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                    AnimatedVisibility(
                        visible = expandedCategoryId == group.id,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Column(
                            modifier = Modifier.padding(start = 14.dp, top = 4.dp, bottom = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            group.subcategories.forEach { subcategory ->
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (selectedSubcategoryId == subcategory.id) {
                                        Terracotta.copy(alpha = 0.12f)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCategoryId = group.id
                                            selectedSubcategoryId = subcategory.id
                                            onCategorySelection(group.id, subcategory.id)
                                        },
                                ) {
                                    Text(
                                        text = subcategory.title,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (selectedSubcategoryId == subcategory.id) Terracotta else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBarIconSurface(
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
        ),
    ) {
        Box(
            modifier = Modifier
                .padding(6.dp)
                .size(42.dp),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
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
    onRetry: () -> Unit,
    pendingFavoriteIds: Set<String>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Cream),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        uiState.currentUser?.let { user ->
            item {
                HomeAccountCard(
                    user = user,
                    status = uiState.accountStatus,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                )
            }
        }

        uiState.error?.let { message ->
            item {
                HomeInlineErrorCard(
                    message = message,
                    onRetry = onRetry,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                )
            }
        }

        // Categories row
        if (uiState.categories.isNotEmpty()) {
            item {
                CategoriesRow(
                    categories = uiState.categories,
                    onCategoryClick = onCategoryClick,
                )
            }
        }

        // Hero banner
        if (uiState.banner != null) item {
            uiState.banner?.let { banner ->
                BannerSection(
                    banner = banner,
                    onCtaClick = {},
                )
            }
            Spacer(Modifier.height(36.dp))
        }

        // Featured artifacts
        if (uiState.featuredProducts.isNotEmpty()) item {
            FeaturedProductsSection(
                products = uiState.featuredProducts,
                canUseWishlist = uiState.canUseWishlist,
                onProductClick = onProductClick,
                onFavoriteToggle = onFavoriteToggle,
                pendingFavoriteIds = pendingFavoriteIds,
                onViewAll = onViewAllFeatured,
            )
            Spacer(Modifier.height(40.dp))
        }

        // New Arrivals
        if (uiState.newArrivals.isNotEmpty()) item {
            Column {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = stringResource(R.string.home_freshly_harvested),
                        style = MaterialTheme.typography.labelMedium,
                        color = StoneGray,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.home_new_arrivals),
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

@Composable
private fun HomeAccountCard(
    user: User,
    status: com.example.myappmobile.domain.model.SellerApprovalStatus,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = White,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FloraRemoteImage(
                imageUrl = user.avatarUrl,
                contentDescription = user.fullName,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CreamDark),
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = user.fullName.ifBlank { "FLORA Member" },
                    style = MaterialTheme.typography.titleLarge,
                    color = CharcoalDark,
                )
                Text(
                    text = user.email.ifBlank { "No email available" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = StoneGray,
                )
                if (user.storeName.isNotBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Storefront,
                            contentDescription = null,
                            tint = Terracotta,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = user.storeName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Terracotta,
                        )
                    }
                }
            }
            SellerApprovalBadge(status = status)
        }
    }
}

@Composable
private fun HomeErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cream)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Unable to load the FLORA home page.",
            style = MaterialTheme.typography.headlineSmall,
            color = CharcoalDark,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = StoneGray,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            text = "Retry",
            onClick = onRetry,
        )
    }
}

@Composable
private fun HomeInlineErrorCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = White,
        tonalElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Some sections could not be refreshed.",
                style = MaterialTheme.typography.titleMedium,
                color = CharcoalDark,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = StoneGray,
            )
            PrimaryButton(
                text = "Retry",
                onClick = onRetry,
                fillMaxWidth = false,
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
        FloraRemoteImage(
            imageUrl = product.imageUrl,
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
            text = stringResource(R.string.home_join_atelier),
            style = MaterialTheme.typography.headlineMedium,
            color = CharcoalDark,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.home_join_atelier_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = StoneGray,
        )
        Spacer(Modifier.height(20.dp))

        if (isSubscribed) {
            Text(
                text = stringResource(R.string.home_join_thank_you),
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
                                text = stringResource(R.string.common_email_address_placeholder),
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
                    contentDescription = stringResource(R.string.home_subscribe),
                    tint = Terracotta,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onSubscribe),
                )
            }
        }
    }
}

@Composable
private fun localizedHomeCategory(category: Category): String = when (category.id) {
    "jewelry" -> stringResource(R.string.category_jewelry)
    "home" -> stringResource(R.string.category_home)
    "textiles" -> stringResource(R.string.category_textiles)
    "ceramics" -> stringResource(R.string.category_ceramics)
    else -> category.name
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
