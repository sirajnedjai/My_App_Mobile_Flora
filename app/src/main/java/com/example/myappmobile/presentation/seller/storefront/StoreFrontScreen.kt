package com.example.myappmobile.presentation.seller.storefront

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Window
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myappmobile.R
import com.example.myappmobile.core.components.AtelierDivider
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.components.SellerApprovalBadge
import com.example.myappmobile.core.components.SmallActionButton
import com.example.myappmobile.core.components.StarRatingRow
import com.example.myappmobile.core.navigation.AppBottomBar
import com.example.myappmobile.core.theme.Cream
import com.example.myappmobile.core.theme.CreamDark
import com.example.myappmobile.core.theme.FloraSelectedCard
import com.example.myappmobile.core.theme.FloraTheme
import com.example.myappmobile.core.theme.SerifFontFamily
import com.example.myappmobile.core.theme.StoneGray
import com.example.myappmobile.core.theme.Terracotta
import com.example.myappmobile.domain.model.Product
import com.example.myappmobile.domain.model.Review
import com.example.myappmobile.domain.model.Store

@Composable
fun StoreFrontScreen(
    onBack: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onOpenReviews: () -> Unit = {},
    onProductClick: (String) -> Unit = {},
    selectedRoute: String = "seller",
    onBottomNavClick: (String) -> Unit = {},
    viewModel: StoreFrontViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SellerFrontScreenContent(
        uiState = uiState,
        onBack = onBack,
        onContactSeller = onOpenAbout,
        onOpenReviews = onOpenReviews,
        onProductClick = onProductClick,
        onToggleProductLayout = viewModel::onToggleProductLayout,
        selectedRoute = selectedRoute,
        onBottomNavClick = onBottomNavClick,
    )
}

@Composable
private fun SellerFrontScreenContent(
    uiState: StoreFrontUiState,
    onBack: () -> Unit,
    onContactSeller: () -> Unit,
    onOpenReviews: () -> Unit,
    onProductClick: (String) -> Unit,
    onToggleProductLayout: () -> Unit,
    selectedRoute: String,
    onBottomNavClick: (String) -> Unit,
) {
    val store = uiState.store ?: return

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SellerFrontTopBar(
                onBack = onBack,
                isAlternateProductLayout = uiState.isAlternateProductLayout,
                onToggleProductLayout = onToggleProductLayout,
            )
        },
        bottomBar = {
            AppBottomBar(
                selectedRoute = selectedRoute,
                onNavigate = onBottomNavClick,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            item {
                SellerHeaderSection(
                    store = store,
                    onContactSeller = onContactSeller,
                )
            }

            item {
                SellerHeroImageSection(store = store)
            }

            item {
                StorySection(store = store)
            }

            item {
                CuratedWorksSectionHeader()
            }

            item {
                CuratedWorksSection(
                    products = uiState.products,
                    isAlternateLayout = uiState.isAlternateProductLayout,
                    onProductClick = onProductClick,
                )
            }

            item {
                TestimonialsSection(
                    reviews = uiState.reviews,
                    onOpenReviews = onOpenReviews,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerFrontTopBar(
    onBack: () -> Unit,
    isAlternateProductLayout: Boolean,
    onToggleProductLayout: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "FLORA",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = MaterialTheme.typography.labelLarge.letterSpacing * 0.75f,
                ),
            )
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
                icon = Icons.Outlined.Window,
                contentDescription = if (isAlternateProductLayout) {
                    stringResource(R.string.storefront_show_editorial_cards)
                } else {
                    stringResource(R.string.storefront_show_compact_grid)
                },
                onClick = onToggleProductLayout,
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

@Composable
private fun SellerHeaderSection(
    store: Store,
    onContactSeller: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = store.logoUrl.ifBlank { store.bannerUrl },
            contentDescription = store.name,
            modifier = Modifier
                .size(124.dp)
                .clip(CircleShape)
                .background(CreamDark),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = store.ownerName.ifBlank { store.name },
            style = MaterialTheme.typography.displayMedium.copy(
                fontFamily = SerifFontFamily,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
            ),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StarRatingRow(rating = store.rating)
            Text(
                text = stringResource(R.string.storefront_reviews_count, store.rating.toString(), store.reviewCount),
                style = MaterialTheme.typography.bodySmall.copy(color = StoneGray),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = store.categories.joinToString(" · ").uppercase().ifBlank { "ARTISAN STUDIO" },
            style = MaterialTheme.typography.labelMedium.copy(color = Terracotta),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        SellerApprovalBadge(status = store.approvalStatus)
        Spacer(modifier = Modifier.height(22.dp))
        SellerActionButtons(
            onContactSeller = onContactSeller,
        )
    }
}

@Composable
private fun SellerActionButtons(
    onContactSeller: () -> Unit,
) {
    PrimaryButton(
        text = stringResource(R.string.storefront_contact_seller),
        onClick = onContactSeller,
        leadingIcon = Icons.Outlined.MailOutline,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SellerHeroImageSection(
    store: Store,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        AsyncImage(
            model = store.bannerUrl.ifBlank { store.logoUrl },
            contentDescription = "${store.name} banner",
            modifier = Modifier
                .fillMaxWidth()
                .height(330.dp),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun StorySection(
    store: Store,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionTitle(title = stringResource(R.string.product_story_label))
        Text(
            text = store.story.ifBlank { store.description },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.82f),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.storefront_crafting_since),
                style = MaterialTheme.typography.labelMedium.copy(color = StoneGray),
            )
            Text(
                text = store.practisingSince.ifBlank { "2016" },
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = SerifFontFamily),
            )
        }
    }
}

@Composable
private fun CuratedWorksSectionHeader(
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AtelierDivider()
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SectionTitle(title = stringResource(R.string.storefront_curated_works))
            Text(
                text = stringResource(R.string.storefront_curated_works_subtitle),
                style = MaterialTheme.typography.bodySmall.copy(color = StoneGray),
            )
        }
    }
}

@Composable
private fun CuratedWorksSection(
    products: List<Product>,
    isAlternateLayout: Boolean,
    onProductClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(if (isAlternateLayout) 14.dp else 18.dp)) {
        if (isAlternateLayout) {
            products.chunked(2).forEach { rowProducts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    rowProducts.forEach { product ->
                        ProductWorkCard(
                            product = product,
                            onClick = { onProductClick(product.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowProducts.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            products.forEach { product ->
                EditorialProductCard(
                    product = product,
                    onClick = { onProductClick(product.id) },
                )
            }
        }
    }
}

@Composable
private fun EditorialProductCard(
    product: Product,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(208.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .weight(1.05f)
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ProductBadge(text = product.category.uppercase())
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = SerifFontFamily,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = product.description.ifBlank { "Hand-finished for the FLORA storefront." },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = product.studio.ifBlank { "FLORA ATELIER" },
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = StoneGray,
                            letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing * 0.9f,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "$${"%.2f".format(product.price)}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Terracotta,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductBadge(
    text: String,
) {
    Surface(
        color = Terracotta.copy(alpha = 0.12f),
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Terracotta,
            maxLines = 1,
        )
    }
}

@Composable
private fun ProductWorkCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        Column {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(184.dp),
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = product.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Terracotta,
                    maxLines = 1,
                )
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = SerifFontFamily,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                )
                Text(
                    text = product.description.ifBlank { product.category.uppercase() },
                    style = MaterialTheme.typography.bodySmall.copy(color = StoneGray),
                    maxLines = 2,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "$${"%.2f".format(product.price)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Terracotta,
                    )
                    Text(
                        text = product.studio.ifBlank { "FLORA" },
                        style = MaterialTheme.typography.labelSmall.copy(color = StoneGray),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun TestimonialsSection(
    reviews: List<Review>,
    onOpenReviews: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AtelierDivider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SectionTitle(title = stringResource(R.string.storefront_voices_from_atelier))
                Text(
                    text = stringResource(R.string.storefront_reflections_from_collectors),
                    style = MaterialTheme.typography.bodySmall.copy(color = StoneGray),
                )
            }
            SmallActionButton(
                text = stringResource(R.string.storefront_all_reviews),
                onClick = onOpenReviews,
            )
        }

        reviews.take(2).forEach { review ->
            TestimonialCard(review = review)
        }
    }
}

@Composable
private fun TestimonialCard(
    review: Review,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "\"${review.text.trim().trim('"')}\"",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = SerifFontFamily,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.15f,
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.82f),
            )
            Text(
                text = review.authorName,
                style = MaterialTheme.typography.labelMedium.copy(color = Terracotta),
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = SerifFontFamily,
            fontStyle = FontStyle.Italic,
        ),
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Preview(showBackground = true)
@Composable
private fun SellerFrontScreenPreview() {
    FloraTheme {
        Surface(color = Cream) {
            SellerFrontScreenContent(
                uiState = StoreFrontUiState(
                    store = Store(
                        id = "s1",
                        name = "FLORA Ceramics",
                        ownerName = "Sienna Moretti",
                        description = "Hand-thrown ceramics inspired by the Wabi-Sabi philosophy.",
                        logoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
                        bannerUrl = "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800",
                        location = "Florence, Italy",
                        contactEmail = "sienna@flora.com",
                        rating = 4.8f,
                        reviewCount = 100,
                        practisingSince = "2012",
                        categories = listOf("Ceramics", "Textiles"),
                        story = "Based in the sun-soaked valleys of Tuscany, Sienna Moretti's work is a dialogue between raw earth and human touch. Each ceramic vessel is hand-thrown using local clays, and every textile piece is woven for slow living.",
                    ),
                    products = listOf(
                        Product(
                            id = "p1",
                            name = "The Sculpted Ripple Vase",
                            price = 185.0,
                            imageUrl = "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=800",
                            studio = "FLORA CERAMICS",
                            category = "Ceramics",
                            description = "Hand-thrown stoneware / limited batch",
                        ),
                        Product(
                            id = "p2",
                            name = "Wabi-Sabi Cereal Bowl",
                            price = 65.0,
                            imageUrl = "https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=600",
                            studio = "FLORA CERAMICS",
                            category = "Ceramics",
                            description = "Crackle glaze stoneware",
                        ),
                    ),
                    reviews = listOf(
                        Review(
                            id = "r1",
                            authorName = "ELENA R. — PATRON",
                            rating = 5,
                            text = "The texture of the linen is unlike anything I've owned. It feels alive.",
                            isVerified = true,
                        ),
                        Review(
                            id = "r2",
                            authorName = "JULIAN R. PORTLAND",
                            rating = 5,
                            text = "The vase arrived perfectly packaged and now anchors my dining room beautifully.",
                            isVerified = true,
                        ),
                    ),
                ),
                onBack = {},
                onContactSeller = {},
                onOpenReviews = {},
                onProductClick = {},
                onToggleProductLayout = {},
                selectedRoute = "seller",
                onBottomNavClick = {},
            )
        }
    }
}
