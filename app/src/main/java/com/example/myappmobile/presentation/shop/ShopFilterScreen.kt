package com.example.myappmobile.presentation.shop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.catalog.FloraCatalog
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.di.AppContainer
import com.example.myappmobile.core.theme.Cream
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraCardBg
import com.example.myappmobile.core.theme.FloraDivider
import com.example.myappmobile.core.theme.FloraSelectedCard
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.FloraWhite
import com.example.myappmobile.core.theme.SerifFontFamily
import com.example.myappmobile.core.theme.StoneFaint
import com.example.myappmobile.core.theme.StoneGray
import com.example.myappmobile.core.theme.Terracotta
import com.example.myappmobile.data.repository.ShopFilterSelection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt

private const val MIN_PRICE_BOUND = 0f
private const val MAX_PRICE_BOUND = 1000f

data class ShopFilterUiState(
    val categoryId: String = "",
    val subcategoryId: String = "",
    val type: String = "All",
    val minPrice: String = "",
    val maxPrice: String = "",
    val minPriceValue: Float = MIN_PRICE_BOUND,
    val maxPriceValue: Float = MAX_PRICE_BOUND,
    val errorMessage: String? = null,
) {
    val activeSelectionCount: Int
        get() = listOf(
            categoryId.isNotBlank(),
            subcategoryId.isNotBlank(),
            type != "All",
            minPriceValue > MIN_PRICE_BOUND || maxPriceValue < MAX_PRICE_BOUND,
        ).count { it }
}

class ShopFilterViewModel : ViewModel() {
    private val repository = AppContainer.shopFilterRepository

    private val _uiState = MutableStateFlow(repository.filters.value.toUiState())
    val uiState: StateFlow<ShopFilterUiState> = _uiState.asStateFlow()

    fun onCategorySelected(categoryId: String) {
        _uiState.update {
            val nextCategory = if (it.categoryId == categoryId) "" else categoryId
            it.copy(
                categoryId = nextCategory,
                subcategoryId = "",
                errorMessage = null,
            )
        }
    }

    fun onSubcategorySelected(subcategoryId: String) {
        _uiState.update {
            it.copy(
                subcategoryId = if (it.subcategoryId == subcategoryId) "" else subcategoryId,
                errorMessage = null,
            )
        }
    }

    fun onTypeSelected(type: String) {
        _uiState.update {
            it.copy(
                type = if (it.type == type) "All" else type,
                errorMessage = null,
            )
        }
    }

    fun onPriceRangeChanged(start: Float, end: Float) {
        val safeStart = start.coerceIn(MIN_PRICE_BOUND, MAX_PRICE_BOUND).coerceAtMost(end)
        val safeEnd = end.coerceIn(MIN_PRICE_BOUND, MAX_PRICE_BOUND).coerceAtLeast(safeStart)
        _uiState.update {
            it.copy(
                minPriceValue = safeStart,
                maxPriceValue = safeEnd,
                minPrice = safeStart.roundToInt().toString(),
                maxPrice = safeEnd.roundToInt().toString(),
                errorMessage = null,
            )
        }
    }

    fun onMinPriceChanged(value: String) {
        val sanitized = value.filter { it.isDigit() }
        _uiState.update { state ->
            val parsed = sanitized.toFloatOrNull()?.coerceIn(MIN_PRICE_BOUND, MAX_PRICE_BOUND)
            state.copy(
                minPrice = sanitized,
                minPriceValue = parsed ?: MIN_PRICE_BOUND,
                errorMessage = null,
            )
        }
    }

    fun onMaxPriceChanged(value: String) {
        val sanitized = value.filter { it.isDigit() }
        _uiState.update { state ->
            val parsed = sanitized.toFloatOrNull()?.coerceIn(MIN_PRICE_BOUND, MAX_PRICE_BOUND)
            state.copy(
                maxPrice = sanitized,
                maxPriceValue = parsed ?: MAX_PRICE_BOUND,
                errorMessage = null,
            )
        }
    }

    fun resetFilters() {
        _uiState.value = ShopFilterUiState()
        repository.clear()
    }

    fun applyFilters(onApplied: () -> Unit) {
        val min = uiState.value.minPrice.ifBlank { uiState.value.minPriceValue.roundToInt().toString() }.toDoubleOrNull()
        val max = uiState.value.maxPrice.ifBlank { uiState.value.maxPriceValue.roundToInt().toString() }.toDoubleOrNull()
        if (min != null && max != null && min > max) {
            _uiState.update { it.copy(errorMessage = "Minimum price must be less than or equal to maximum price.") }
            return
        }
        repository.apply(
            ShopFilterSelection(
                categoryId = uiState.value.categoryId,
                subcategoryId = uiState.value.subcategoryId,
                type = uiState.value.type,
                minPrice = min?.roundToInt()?.toString().orEmpty(),
                maxPrice = max?.roundToInt()?.toString().orEmpty(),
            ),
        )
        onApplied()
    }

    private fun ShopFilterSelection.toUiState(): ShopFilterUiState {
        val parsedMin = minPrice.toFloatOrNull()?.coerceIn(MIN_PRICE_BOUND, MAX_PRICE_BOUND) ?: MIN_PRICE_BOUND
        val parsedMax = maxPrice.toFloatOrNull()?.coerceIn(MIN_PRICE_BOUND, MAX_PRICE_BOUND) ?: MAX_PRICE_BOUND
        return ShopFilterUiState(
            categoryId = categoryId,
            subcategoryId = subcategoryId,
            type = type,
            minPrice = minPrice.ifBlank { parsedMin.roundToInt().takeIf { parsedMin > MIN_PRICE_BOUND }?.toString().orEmpty() },
            maxPrice = maxPrice.ifBlank { parsedMax.roundToInt().takeIf { parsedMax < MAX_PRICE_BOUND }?.toString().orEmpty() },
            minPriceValue = parsedMin,
            maxPriceValue = parsedMax,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShopFilterScreen(
    onBack: () -> Unit,
    onApplied: () -> Unit,
    viewModel: ShopFilterViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedGroup = FloraCatalog.groupFor(uiState.categoryId)

    Scaffold(
        topBar = {
            FilterTopBar(
                onBack = onBack,
                onReset = viewModel::resetFilters,
            )
        },
        bottomBar = {
            FilterBottomBar(
                hasActiveFilters = uiState.activeSelectionCount > 0,
                onApply = { viewModel.applyFilters(onApplied) },
                onClear = viewModel::resetFilters,
            )
        },
        containerColor = FloraBeige,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(FloraBeige)
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                FilterHeroCard(
                    activeSelectionCount = uiState.activeSelectionCount,
                    minPrice = uiState.minPriceValue.roundToInt(),
                    maxPrice = uiState.maxPriceValue.roundToInt(),
                )
            }

            item {
                FilterSectionCard(
                    title = "Category",
                    subtitle = "Choose the atelier family you want to explore first.",
                    icon = Icons.Outlined.Layers,
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        FloraCatalog.categoryGroups.forEach { group ->
                            LuxuryChip(
                                text = group.title,
                                selected = uiState.categoryId == group.id,
                                onClick = { viewModel.onCategorySelected(group.id) },
                            )
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = selectedGroup != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    selectedGroup?.let { group ->
                        FilterSectionCard(
                            title = "Type",
                            subtitle = "Refine ${group.title.lowercase()} by subcategory.",
                            icon = Icons.Outlined.LocalOffer,
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                group.subcategories.forEach { subcategory ->
                                    LuxuryChip(
                                        text = subcategory.title,
                                        selected = uiState.subcategoryId == subcategory.id,
                                        onClick = { viewModel.onSubcategorySelected(subcategory.id) },
                                        compact = true,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                FilterSectionCard(
                    title = "Collection Type",
                    subtitle = "Layer merchandising cues like premium, limited, or under-budget edits.",
                    icon = Icons.Outlined.AutoAwesome,
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        FloraCatalog.quickFilterTypes.forEach { type ->
                            LuxuryChip(
                                text = type,
                                selected = uiState.type == type,
                                onClick = { viewModel.onTypeSelected(type) },
                                compact = true,
                            )
                        }
                    }
                }
            }

            item {
                FilterSectionCard(
                    title = "Price Range",
                    subtitle = "Move through a curated budget with an intuitive luxury range selector.",
                    icon = Icons.Outlined.Sell,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        PriceRangeHighlight(
                            minPrice = uiState.minPriceValue.roundToInt(),
                            maxPrice = uiState.maxPriceValue.roundToInt(),
                        )

                        RangeSlider(
                            value = uiState.minPriceValue..uiState.maxPriceValue,
                            onValueChange = { viewModel.onPriceRangeChanged(it.start, it.endInclusive) },
                            valueRange = MIN_PRICE_BOUND..MAX_PRICE_BOUND,
                            colors = androidx.compose.material3.SliderDefaults.colors(
                                activeTrackColor = FloraBrown,
                                inactiveTrackColor = FloraDivider,
                                activeTickColor = Color.Transparent,
                                inactiveTickColor = Color.Transparent,
                                thumbColor = FloraWhite,
                            ),
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CurrencyField(
                                label = "Minimum",
                                value = uiState.minPrice,
                                placeholder = "0",
                                onValueChange = viewModel::onMinPriceChanged,
                                modifier = Modifier.weight(1f),
                            )
                            CurrencyField(
                                label = "Maximum",
                                value = uiState.maxPrice,
                                placeholder = "1000",
                                onValueChange = viewModel::onMaxPriceChanged,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        AnimatedVisibility(visible = uiState.errorMessage != null) {
                            uiState.errorMessage?.let { error ->
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterTopBar(
    onBack: () -> Unit,
    onReset: () -> Unit,
) {
    TopAppBar(
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Filter Products",
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = SerifFontFamily),
                    color = FloraText,
                )
                Text(
                    text = "Refine your selection",
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
            }
        },
        navigationIcon = {
            CircularIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
            )
        },
        actions = {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = FloraSelectedCard,
                border = BorderStroke(1.dp, FloraDivider.copy(alpha = 0.8f)),
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickable(onClick = onReset),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        tint = FloraTextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.labelLarge,
                        color = FloraTextSecondary,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = FloraBeige,
            scrolledContainerColor = FloraBeige,
        ),
    )
}

@Composable
private fun FilterHeroCard(
    activeSelectionCount: Int,
    minPrice: Int,
    maxPrice: Int,
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8F3EE),
                            Cream,
                        ),
                    ),
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Terracotta.copy(alpha = 0.10f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        tint = Terracotta,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = if (activeSelectionCount == 0) {
                            "No active filters"
                        } else {
                            "$activeSelectionCount filters selected"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = Terracotta,
                    )
                }
            }

            Text(
                text = "Shape the collection with quiet precision and return to the shop with a cleaner, more intentional result.",
                style = MaterialTheme.typography.bodyMedium,
                color = FloraText,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryPill(title = "Range", value = "$$minPrice - $$maxPrice")
                SummaryPill(title = "Mood", value = if (activeSelectionCount == 0) "Open" else "Curated")
            }
        }
    }
}

@Composable
private fun SummaryPill(
    title: String,
    value: String,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = FloraSelectedCard,
        border = BorderStroke(1.dp, FloraDivider.copy(alpha = 0.7f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = StoneGray,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = FloraText,
            )
        }
    }
}

@Composable
private fun FilterSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = Terracotta.copy(alpha = 0.10f),
                ) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Terracotta,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = FloraText,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = FloraTextSecondary,
                    )
                }
            }

            HorizontalDivider(color = FloraDivider.copy(alpha = 0.55f))
            content()
        }
    }
}

@Composable
private fun LuxuryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    compact: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.985f,
        label = "chipScale",
    )

    Surface(
        shape = RoundedCornerShape(if (compact) 18.dp else 22.dp),
        color = if (selected) Terracotta.copy(alpha = 0.13f) else FloraSelectedCard,
        tonalElevation = 0.dp,
        shadowElevation = if (selected) 5.dp else 1.dp,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) Terracotta.copy(alpha = 0.45f) else FloraDivider.copy(alpha = 0.8f),
        ),
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = if (compact) 14.dp else 16.dp, vertical = if (compact) 10.dp else 12.dp),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            ),
            color = if (selected) Terracotta else FloraText,
        )
    }
}

@Composable
private fun PriceRangeHighlight(
    minPrice: Int,
    maxPrice: Int,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        PriceBadge(
            label = "From",
            value = "$$minPrice",
            modifier = Modifier.weight(1f),
        )
        PriceBadge(
            label = "To",
            value = "$$maxPrice",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PriceBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = FloraCardBg,
        border = BorderStroke(1.dp, FloraDivider.copy(alpha = 0.75f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = StoneGray,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = SerifFontFamily,
                    fontStyle = FontStyle.Italic,
                ),
                color = FloraText,
            )
        }
    }
}

@Composable
private fun CurrencyField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        label = {
            Text(label)
        },
        leadingIcon = {
            Surface(
                shape = CircleShape,
                color = Terracotta.copy(alpha = 0.10f),
            ) {
                Box(
                    modifier = Modifier.size(28.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$",
                        style = MaterialTheme.typography.labelLarge,
                        color = Terracotta,
                    )
                }
            }
        },
        placeholder = { Text(placeholder) },
        shape = RoundedCornerShape(22.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = FloraSelectedCard,
            unfocusedContainerColor = FloraSelectedCard,
            focusedBorderColor = FloraBrown,
            unfocusedBorderColor = FloraDivider,
            cursorColor = FloraBrown,
            focusedTextColor = FloraText,
            unfocusedTextColor = FloraText,
            focusedLabelColor = FloraBrown,
            unfocusedLabelColor = FloraTextSecondary,
        ),
    )
}

@Composable
private fun FilterBottomBar(
    hasActiveFilters: Boolean,
    onApply: () -> Unit,
    onClear: () -> Unit,
) {
    Surface(
        color = FloraBeige,
        shadowElevation = 18.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LuxuryPrimaryActionButton(
                text = "Apply Filters",
                onClick = onApply,
            )
            LuxurySecondaryActionButton(
                text = "Clear All",
                enabled = hasActiveFilters,
                onClick = onClear,
            )
        }
    }
}

@Composable
private fun LuxuryPrimaryActionButton(
    text: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(targetValue = 1f, label = "primaryButtonScale")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 12.dp,
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            FloraBrown,
                            Terracotta,
                        ),
                    ),
                )
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = FloraWhite,
            )
        }
    }
}

@Composable
private fun LuxurySecondaryActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = if (enabled) FloraSelectedCard else StoneFaint,
        border = BorderStroke(1.dp, FloraDivider),
    ) {
        Box(
            modifier = Modifier.padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                color = if (enabled) FloraTextSecondary else StoneGray,
            )
        }
    }
}
