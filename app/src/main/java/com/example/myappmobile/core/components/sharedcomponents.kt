package com.example.myappmobile.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myappmobile.core.theme.*

// ─── Favorite Toggle Button ───────────────────────────────────────────────────

@Composable
fun FavoriteButton(
    isFavorited: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconColor by animateColorAsState(
        targetValue = if (isFavorited) ErrorRed else CharcoalDark,
        label = "favColor",
    )
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(White.copy(alpha = 0.85f))
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorited) "Remove from favorites" else "Add to favorites",
            tint = iconColor,
            modifier = Modifier.size(18.dp),
        )
    }
}

// ─── Star Rating Row ─────────────────────────────────────────────────────────

@Composable
fun StarRatingRow(
    rating: Float,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (index < rating.toInt()) StarGold else StoneLight,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

// ─── Section Header ──────────────────────────────────────────────────────────

@Composable
fun SectionHeader(
    title: String,
    ctaText: String? = null,
    onCtaClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = CharcoalDark,
        )
        if (ctaText != null) {
            Text(
                text = ctaText,
                style = MaterialTheme.typography.labelMedium.copy(color = Terracotta),
                modifier = Modifier.clickable(onClick = onCtaClick),
            )
        }
    }
}

// ─── Divider ─────────────────────────────────────────────────────────────────

@Composable
fun AtelierDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 0.5.dp,
        color = StoneFaint,
    )
}

// ─── Shimmer / Loading Placeholder ───────────────────────────────────────────

@Composable
fun ShimmerBox(
    height: Dp = 200.dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(MaterialTheme.shapes.large)
            .background(StoneFaint),
    )
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "FavoriteButton — Active")
@Composable
fun FavoriteButtonActivePreview() {
    AtelierTheme {
        Box(modifier = Modifier.padding(16.dp).background(Terracotta)) {
            FavoriteButton(isFavorited = true, onToggle = {})
        }
    }
}

@Preview(showBackground = true, name = "StarRatingRow")
@Composable
fun StarRatingRowPreview() {
    AtelierTheme {
        StarRatingRow(rating = 4.0f, modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true, name = "SectionHeader")
@Composable
fun SectionHeaderPreview() {
    AtelierTheme {
        SectionHeader(
            title = "Featured Artifacts",
            ctaText = "View all",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "ShimmerBox")
@Composable
fun ShimmerBoxPreview() {
    AtelierTheme {
        ShimmerBox(height = 200.dp, modifier = Modifier.padding(16.dp))
    }
}