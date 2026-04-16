package com.example.myappmobile.presentation.home.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myappmobile.core.theme.*
import com.example.myappmobile.data.MockData
import com.example.myappmobile.domain.Category

private const val LOGIN_DEBUG = "LOGIN_DEBUG"

@Composable
fun CategoriesRow(
    categories: List<Category>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(categories) { category ->
            CategoryChip(
                label = category.name,
                iconRes = category.iconRes,
                onClick = { onCategoryClick(category.id) },
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .width(94.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Terracotta.copy(alpha = 0.12f),
                tonalElevation = 1.dp,
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(color = Terracotta.copy(alpha = 0.04f)),
                    contentAlignment = Alignment.Center,
                ) {
                    val painter = runCatching { painterResource(id = iconRes) }
                        .onFailure { throwable ->
                            Log.e(
                                LOGIN_DEBUG,
                                "Category icon load failed for label=$label iconRes=$iconRes. Falling back safely.",
                                throwable,
                            )
                        }
                        .getOrNull()

                    if (painter != null) {
                        Icon(
                            painter = painter,
                            contentDescription = label,
                            tint = Terracotta,
                            modifier = Modifier.size(24.dp),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Storefront,
                            contentDescription = label,
                            tint = Terracotta,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8)
@Composable
fun CategoriesRowPreview() {
    AtelierTheme {
        CategoriesRow(
            categories = MockData.categories,
            onCategoryClick = {},
        )
    }
}
