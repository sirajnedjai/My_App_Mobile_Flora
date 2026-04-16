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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myappmobile.core.components.AtelierDivider
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.OutlineButton
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.components.ReviewEligibilityNotice
import com.example.myappmobile.core.components.SellerApprovalBadge
import com.example.myappmobile.core.components.ShimmerBox
import com.example.myappmobile.core.components.SmallActionButton
import com.example.myappmobile.core.components.StarRatingRow
import com.example.myappmobile.R
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
    onRatingSelected: (Int) -> Unit = {},
    onReviewInputChanged: (String) -> Unit = {},
    onSubmitReview: () -> Unit = {},
) {
    Scaffold(
        topBar = { ProductDetailsTopBar(onBack = onBack, onCartClick = onCartClick) },
        containerColor = MaterialTheme.colorScheme.background,
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
                    canWriteReviews = uiState.canWriteReviews,
                    restrictionMessage = uiState.restrictionMessage,
                    selectedRating = uiState.selectedRating,
                    reviewInput = uiState.reviewInput,
                    reviewError = uiState.reviewError,
                    reviewSuccess = uiState.reviewSuccess,
                    onRatingSelected = onRatingSelected,
                    onReviewInputChanged = onReviewInputChanged,
                    onSubmitReview = onSubmitReview,
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
            CircularIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.common_back),
                onClick = onBack,
            )
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
            CircularIconButton(
                icon = Icons.Outlined.ShoppingBag,
                contentDescription = stringResource(R.string.common_cart),
                onClick = onCartClick,
            )
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
    canWriteReviews: Boolean,
    restrictionMessage: String?,
    selectedRating: Int,
    reviewInput: String,
    reviewError: String?,
    reviewSuccess: String?,
    onRatingSelected: (Int) -> Unit,
    onReviewInputChanged: (String) -> Unit,
    onSubmitReview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val averageRating = product.reviews.map { it.rating }.average().takeIf { !it.isNaN() }?.toFloat() ?: 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
                    text = stringResource(R.string.product_story_label),
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
                    text = if (addedToCart) stringResource(R.string.product_added_to_cart) else stringResource(R.string.common_add_to_cart),
                    onClick = onAddToCart,
                    enabled = !addedToCart,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlineButton(
                    text = stringResource(R.string.product_reserve_pickup),
                    onClick = onReservePickup,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(40.dp))
        }

        // Reviews header
        item {
            AtelierDivider(modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(24.dp))
            ReviewsHeader(
                averageRating = averageRating,
                reviewCount = product.reviews.size,
                subtitle = stringResource(R.string.product_reviews_subtitle),
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(20.dp))
        }

        item {
            if (canWriteReviews) {
                ReviewComposerCard(
                    selectedRating = selectedRating,
                    reviewInput = reviewInput,
                    errorMessage = reviewError,
                    successMessage = reviewSuccess,
                    onRatingSelected = onRatingSelected,
                    onReviewInputChanged = onReviewInputChanged,
                    onSubmitReview = onSubmitReview,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            } else {
                ReviewEligibilityNotice(
                    message = restrictionMessage
                        ?: stringResource(R.string.product_review_restriction_message),
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
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
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.product_you_may_also_admire),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(R.string.product_you_may_also_admire_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = StoneGray,
                )
            }
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
    val safeImages = if (images.isEmpty()) listOf("") else images

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Terracotta.copy(alpha = 0.12f),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Collections,
                    contentDescription = null,
                    tint = Terracotta,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.product_gallery),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(R.string.product_gallery_count, selectedIndex + 1, safeImages.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = StoneGray,
                )
            }
        }

        AnimatedContent(
            targetState = safeImages.getOrElse(selectedIndex) { safeImages.first() },
            transitionSpec = {
                fadeIn(animationSpec = tween(280)) togetherWith fadeOut(animationSpec = tween(280))
            },
            label = "mainImage",
        ) { imageUrl ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ) {
                Box {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = productName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(390.dp)
                            .background(CreamDark),
                    )
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        tonalElevation = 2.dp,
                    ) {
                        Text(
                            text = "${selectedIndex + 1}/${safeImages.size}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        if (safeImages.size > 1) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(safeImages.size) { index ->
                    val url = safeImages[index]
                    val isSelected = index == selectedIndex
                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) Terracotta else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                        label = "thumbBorder",
                    )
                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) Terracotta.copy(alpha = 0.12f) else FloraSelectedCard,
                        label = "thumbContainer",
                    )

                    Card(
                        modifier = Modifier
                            .size(width = 88.dp, height = 104.dp)
                            .clickable { onSelect(index) },
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = stringResource(R.string.product_image_number, index + 1),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(62.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(CreamDark),
                            )
                            Text(
                                text = if (isSelected) stringResource(R.string.product_current_view) else stringResource(R.string.product_view_number, index + 1),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Terracotta else StoneGray,
                                maxLines = 1,
                            )
                        }
                    }
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
                text = stringResource(R.string.product_material),
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
                text = stringResource(R.string.product_dimensions),
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
        color = FloraSelectedCard,
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
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(6.dp))
                SellerApprovalBadge(status = artist.sellerApprovalStatus)
                Spacer(Modifier.height(8.dp))
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
            SmallActionButton(
                text = stringResource(R.string.product_visit_studio),
                onClick = onVisitStudio,
                leadingIcon = Icons.Outlined.Storefront,
            )
        }
    }
}

// ─── Reviews Header ───────────────────────────────────────────────────────────

@Composable
private fun ReviewsHeader(
    averageRating: Float,
    reviewCount: Int,
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
                text = stringResource(R.string.product_collector_reviews),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.product_reviews_count, reviewCount),
                style = MaterialTheme.typography.labelMedium,
                color = Terracotta,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = StoneGray,
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = String.format("%.1f", averageRating),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
            )
            StarRatingRow(rating = averageRating)
        }
    }
}

@Composable
private fun ReviewComposerCard(
    selectedRating: Int,
    reviewInput: String,
    errorMessage: String?,
    successMessage: String?,
    onRatingSelected: (Int) -> Unit,
    onReviewInputChanged: (String) -> Unit,
    onSubmitReview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Terracotta.copy(alpha = 0.12f),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EditNote,
                        contentDescription = null,
                        tint = Terracotta,
                        modifier = Modifier.padding(10.dp),
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.product_rate_piece),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = stringResource(R.string.product_rate_piece_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = StoneGray,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                (1..5).forEach { rating ->
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = stringResource(R.string.product_rate_stars, rating),
                        tint = if (rating <= selectedRating) StarGold else StoneGray.copy(alpha = 0.35f),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onRatingSelected(rating) },
                    )
                }
            }

            OutlinedTextField(
                value = reviewInput,
                onValueChange = onReviewInputChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                placeholder = {
                    Text(stringResource(R.string.product_review_placeholder))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Terracotta,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = Terracotta,
                ),
            )

            errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            successMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Terracotta,
                )
            }

            PrimaryButton(
                text = stringResource(R.string.product_submit_review),
                onClick = onSubmitReview,
                leadingIcon = Icons.AutoMirrored.Outlined.Send,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ─── Review Card ──────────────────────────────────────────────────────────────

@Composable
private fun ReviewCard(
    review: Review,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    Text(
                        text = review.authorName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Text(
                    text = review.date.ifBlank { stringResource(R.string.common_recently) },
                    style = MaterialTheme.typography.labelSmall,
                    color = StoneGray,
                )
            }

            Text(
                text = review.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    fontStyle = FontStyle.Italic,
                ),
            )
        }
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
            .width(196.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(184.dp)
                        .background(CreamDark),
                )
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = product.category.name.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Terracotta,
                        maxLines = 1,
                    )
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = product.studio,
                        style = MaterialTheme.typography.bodySmall,
                        color = StoneGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "$${product.price.toInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Terracotta,
                    )
                }
            }
        }
    }
}

// ─── Loading ──────────────────────────────────────────────────────────────────

@Composable
private fun ProductDetailsLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
