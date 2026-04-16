package com.example.myappmobile.presentation.search

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myappmobile.R
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.StarRatingRow
import com.example.myappmobile.core.navigation.AppBottomBar
import com.example.myappmobile.core.theme.CreamDark
import com.example.myappmobile.core.theme.FloraCardBg
import com.example.myappmobile.core.theme.FloraSelectedCard
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextMuted
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.PriceGreen
import com.example.myappmobile.core.theme.SearchButtonEnd
import com.example.myappmobile.core.theme.SearchButtonStart
import com.example.myappmobile.core.theme.SearchChipInactive
import com.example.myappmobile.core.theme.SearchChipText
import com.example.myappmobile.core.theme.SearchContainer
import com.example.myappmobile.core.theme.SearchField
import com.example.myappmobile.core.theme.SearchGradientBottom
import com.example.myappmobile.core.theme.SearchGradientTop
import com.example.myappmobile.core.theme.SerifFontFamily
import com.example.myappmobile.core.theme.StoneGray
import com.example.myappmobile.core.theme.Terracotta
import com.example.myappmobile.domain.Product
import kotlin.math.absoluteValue

@Composable
fun SearchScreen(
    onBack: () -> Unit = {},
    onProductClick: (String) -> Unit = {},
    selectedRoute: String = "search",
    onBottomNavClick: (String) -> Unit = {},
    viewModel: SearchViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { SearchTopBar(onBack = onBack) },
        containerColor = Color.Transparent,
        bottomBar = {
            AppBottomBar(
                selectedRoute = selectedRoute,
                onNavigate = onBottomNavClick,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SearchGradientTop,
                            SearchGradientBottom,
                        ),
                    ),
                )
                .padding(padding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .shadow(
                        elevation = 18.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = Color.Black.copy(alpha = 0.08f),
                        spotColor = Color.Black.copy(alpha = 0.08f),
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(SearchContainer)
                    .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(32.dp))
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    item {
                        SearchHeaderBlock(
                            query = uiState.query,
                            onQueryChange = viewModel::onQueryChange,
                            onSearch = viewModel::onSearchSubmitted,
                        )
                    }

                    if (!uiState.hasSearched) {
                        if (uiState.recentSearches.isNotEmpty()) {
                            item {
                                SearchHistoryList(
                                    values = uiState.recentSearches,
                                    onSearchClick = viewModel::onSuggestionSelected,
                                    onDeleteClick = viewModel::removeRecentSearch,
                                    onClearAll = viewModel::clearRecentSearches,
                                )
                            }
                        }

                        item {
                            SearchSuggestionSection(
                                title = stringResource(R.string.search_trending),
                                icon = Icons.AutoMirrored.Outlined.TrendingUp,
                                values = uiState.trendingKeywords.map { localizedKeywordLabel(it) to it },
                                onTagClick = viewModel::onSuggestionSelected,
                            )
                        }

                        item {
                            CategoryQuickSearch(
                                categories = uiState.availableCategories.filterNot { it == "All" },
                                onCategorySelected = {
                                    viewModel.onCategorySelected(it)
                                    viewModel.onSuggestionSelected(it)
                                },
                            )
                        }

                        item {
                            EmptyStateWidget(
                                icon = Icons.Outlined.Search,
                                title = stringResource(R.string.search_intro_title),
                                subtitle = stringResource(R.string.search_intro_subtitle),
                            )
                        }
                    } else {
                        item {
                            SearchControls(
                                categories = uiState.availableCategories,
                                selectedCategory = uiState.selectedCategory,
                                selectedSort = uiState.selectedSort,
                                onCategorySelected = viewModel::onCategorySelected,
                                onSortSelected = viewModel::onSortSelected,
                            )
                        }

                        item {
                            SearchResultsHeader(
                                resultCount = uiState.results.size,
                                isLoading = uiState.isLoading,
                                query = uiState.query,
                            )
                        }

                        item {
                            Crossfade(
                                targetState = when {
                                    uiState.isLoading -> SearchContentState.Loading
                                    uiState.results.isEmpty() -> SearchContentState.Empty
                                    else -> SearchContentState.Results
                                },
                                label = "search_state",
                            ) { state ->
                                when (state) {
                                    SearchContentState.Loading -> SearchLoadingState()
                                    SearchContentState.Empty -> EmptyStateWidget(
                                        icon = Icons.Outlined.Search,
                                        title = stringResource(R.string.search_no_products_title),
                                        subtitle = stringResource(R.string.search_no_products_subtitle),
                                    )
                                    SearchContentState.Results -> ProductGrid(
                                        products = uiState.results,
                                        onProductClick = onProductClick,
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

private enum class SearchContentState {
    Loading,
    Empty,
    Results,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(onBack: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = FloraText,
        ),
        title = {
            Text(
                text = stringResource(R.string.nav_search).lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = SerifFontFamily,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        },
        navigationIcon = {
            CircularIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.common_back),
                onClick = onBack,
            )
        },
    )
}

@Composable
private fun SearchHeaderBlock(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SearchBarWidget(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
        )
        PremiumSearchButton(onClick = onSearch)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBarWidget(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(34.dp)),
        singleLine = true,
        placeholder = {
            Text(
                text = stringResource(R.string.search_page_placeholder),
                color = FloraTextMuted,
            )
        },
        leadingIcon = {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = Terracotta)
        },
        trailingIcon = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SearchContainer.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.GraphicEq,
                    contentDescription = null,
                    tint = Terracotta,
                )
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        shape = RoundedCornerShape(34.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SearchField,
            unfocusedContainerColor = SearchField,
            disabledContainerColor = SearchField,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = FloraText,
            unfocusedTextColor = FloraText,
            cursorColor = Terracotta,
        ),
    )
}

@Composable
private fun PremiumSearchButton(
    onClick: () -> Unit,
) {
    CustomGradientButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 18.dp),
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = Color.White)
        Text(
            text = stringResource(R.string.search_button_label),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
            ),
            color = Color.White,
        )
    }
}

@Composable
private fun CustomGradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 22.dp, vertical = 16.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = MutableInteractionSource()
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "search_gradient_button_scale",
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(34.dp),
                ambientColor = SearchButtonStart.copy(alpha = 0.35f),
                spotColor = SearchButtonStart.copy(alpha = 0.35f),
            )
            .clip(RoundedCornerShape(34.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(SearchButtonStart, SearchButtonEnd),
                ),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
private fun CustomChipButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val interactionSource = MutableInteractionSource()
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "search_chip_button_scale",
    )

    val shape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (isActive) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = shape,
                        ambientColor = SearchButtonStart.copy(alpha = 0.35f),
                        spotColor = SearchButtonStart.copy(alpha = 0.35f),
                    )
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(
                if (isActive) {
                    Brush.linearGradient(
                        colors = listOf(SearchButtonStart, SearchButtonEnd),
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(SearchChipInactive, SearchChipInactive),
                    )
                },
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 11.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.invoke()
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                ),
                color = if (isActive) Color.White else SearchChipText,
            )
        }
    }
}

@Composable
private fun SearchHistoryList(
    values: List<String>,
    onSearchClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onClearAll: () -> Unit,
) {
    PremiumSectionCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.History, contentDescription = null, tint = Terracotta, modifier = Modifier.size(18.dp))
                    Text(
                        text = stringResource(R.string.search_recent_searches),
                        style = MaterialTheme.typography.titleMedium,
                        color = FloraText,
                    )
                }
                CustomChipButton(
                    label = stringResource(R.string.search_clear_all),
                    isActive = false,
                    onClick = onClearAll,
                )
            }
            values.forEach { value ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { onSearchClick(value) }
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Terracotta.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.Search, contentDescription = null, tint = Terracotta, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyLarge,
                            color = FloraText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(onClick = { onDeleteClick(value) }) {
                        Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.common_remove), tint = StoneGray)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchSuggestionSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    values: List<Pair<String, String>>,
    onTagClick: (String) -> Unit,
) {
    PremiumSectionCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = Terracotta, modifier = Modifier.size(18.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = FloraText,
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                values.forEach { (displayValue, rawValue) ->
                    CustomChipButton(
                        label = displayValue,
                        isActive = false,
                        onClick = { onTagClick(rawValue) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryQuickSearch(
    categories: List<String>,
    onCategorySelected: (String) -> Unit,
) {
    PremiumSectionCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = stringResource(R.string.search_quick_categories),
                style = MaterialTheme.typography.titleMedium,
                color = FloraText,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                categories.forEach { category ->
                    CustomChipButton(
                        label = localizedCategoryLabel(category),
                        isActive = false,
                        onClick = { onCategorySelected(category) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = SearchChipText,
                            )
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchControls(
    categories: List<String>,
    selectedCategory: String,
    selectedSort: SearchSortUi,
    onCategorySelected: (String) -> Unit,
    onSortSelected: (SearchSortUi) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PremiumSectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.common_category),
                    style = MaterialTheme.typography.titleMedium,
                    color = FloraText,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    categories.forEach { category ->
                        CustomChipButton(
                            label = localizedCategoryLabel(category),
                            isActive = selectedCategory == category,
                            onClick = { onCategorySelected(category) },
                        )
                    }
                }
            }
        }

        PremiumSectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.common_sort),
                    style = MaterialTheme.typography.titleMedium,
                    color = FloraText,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SearchSortUi.entries.forEach { sort ->
                        CustomChipButton(
                            label = stringResource(sort.labelRes),
                            isActive = selectedSort == sort,
                            onClick = { onSortSelected(sort) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultsHeader(
    resultCount: Int,
    isLoading: Boolean,
    query: String,
) {
    PremiumSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.search_results),
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = SerifFontFamily),
                    color = FloraText,
                )
                Text(
                    text = when {
                        isLoading -> stringResource(R.string.search_refreshing_matches)
                        resultCount == 0 -> stringResource(R.string.search_no_results_query, query.ifBlank { stringResource(R.string.nav_search) })
                        else -> stringResource(R.string.search_results_count, resultCount)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = FloraTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun SearchLoadingState() {
    PremiumSectionCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CircularProgressIndicator(color = Terracotta, strokeWidth = 2.5.dp)
            Text(
                text = stringResource(R.string.search_loading_title),
                style = MaterialTheme.typography.titleMedium,
                color = FloraText,
            )
            Text(
                text = stringResource(R.string.search_loading_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = FloraTextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EmptyStateWidget(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
) {
    PremiumSectionCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Terracotta.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Terracotta, modifier = Modifier.size(32.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = SerifFontFamily),
                color = FloraText,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = FloraTextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ProductGrid(
    products: List<Product>,
    onProductClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        products.chunked(2).forEach { rowProducts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                rowProducts.forEach { product ->
                    SearchProductCard(
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
    }
}

@Composable
private fun SearchProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(CreamDark),
            )
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Terracotta.copy(alpha = 0.10f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = localizedCategoryLabel(product.category.name).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Terracotta,
                    )
                }
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = FloraText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2,
                )
                Text(
                    text = product.studio,
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StarRatingRow(rating = pseudoRating(product))
                    Text(
                        text = String.format("%.1f", pseudoRating(product)),
                        style = MaterialTheme.typography.labelSmall,
                        color = StoneGray,
                    )
                }
                Text(
                    text = "$${"%.2f".format(product.price)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PriceGreen,
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

@Composable
private fun PremiumSectionCard(
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = SearchContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(30.dp))
                .padding(18.dp),
            content = content,
        )
    }
}

@Composable
private fun localizedCategoryLabel(category: String): String = when (category.lowercase()) {
    "all" -> stringResource(R.string.common_all)
    "jewelry" -> stringResource(R.string.category_jewelry)
    "home" -> stringResource(R.string.category_home)
    "textiles" -> stringResource(R.string.category_textiles)
    "ceramics" -> stringResource(R.string.category_ceramics)
    else -> category
}

@Composable
private fun localizedKeywordLabel(keyword: String): String = when (keyword.lowercase()) {
    "ceramics" -> stringResource(R.string.category_ceramics)
    "handwoven" -> stringResource(R.string.search_keyword_handwoven)
    "pearl" -> stringResource(R.string.search_keyword_pearl)
    "decor" -> stringResource(R.string.search_keyword_decor)
    else -> keyword
}

private fun pseudoRating(product: Product): Float {
    val seed = product.id.hashCode().toUInt().toInt().absoluteValue % 8
    return 4.1f + (seed * 0.1f)
}
