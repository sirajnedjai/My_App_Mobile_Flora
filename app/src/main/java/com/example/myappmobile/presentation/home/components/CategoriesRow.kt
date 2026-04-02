package com.example.myappmobile.presentation.home.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
        modifier = modifier.padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(CreamDark),
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
                    tint = CharcoalMid,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Storefront,
                    contentDescription = label,
                    tint = CharcoalMid,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CharcoalLight,
        )
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
