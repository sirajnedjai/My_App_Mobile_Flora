package com.example.myappmobile.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myappmobile.core.theme.*
import com.example.myappmobile.domain.model.SellerApprovalStatus

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
    CircularIconButton(
        icon = if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
        contentDescription = if (isFavorited) "Remove from favorites" else "Add to favorites",
        onClick = onToggle,
        modifier = modifier.size(42.dp),
        danger = isFavorited,
    )
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

@Composable
fun BuyersOnlyNotice(
    message: String = "This feature is available for buyers only.",
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Buyer Access",
                style = MaterialTheme.typography.titleMedium,
                color = FloraText,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = FloraTextSecondary,
            )
        }
    }
}

@Composable
fun ReviewEligibilityNotice(
    message: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = StatusAmberLight,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = StatusAmber,
                    modifier = Modifier.padding(10.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Verified Buyer Reviews",
                    style = MaterialTheme.typography.titleMedium,
                    color = FloraText,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
            }
        }
    }
}

@Composable
fun SellerApprovalBadge(
    status: SellerApprovalStatus,
    modifier: Modifier = Modifier,
) {
    val palette = when (status) {
        SellerApprovalStatus.APPROVED -> BadgePalette(
            background = StatusGreenLight,
            content = StatusGreen,
            icon = Icons.Filled.Verified,
            label = "Approved Seller",
        )
        SellerApprovalStatus.PENDING -> BadgePalette(
            background = StatusAmberLight,
            content = StatusAmber,
            icon = Icons.Outlined.HourglassTop,
            label = "Not Yet Approved",
        )
        SellerApprovalStatus.REJECTED -> BadgePalette(
            background = StatusRedLight,
            content = StatusRed,
            icon = Icons.Outlined.Info,
            label = "Approval Update Needed",
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = palette.background,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = palette.icon,
                contentDescription = null,
                tint = palette.content,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = palette.label,
                style = MaterialTheme.typography.labelMedium,
                color = palette.content,
            )
        }
    }
}

private data class BadgePalette(
    val background: Color,
    val content: Color,
    val icon: ImageVector,
    val label: String,
)

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
