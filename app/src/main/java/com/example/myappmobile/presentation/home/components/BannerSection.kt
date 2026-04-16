package com.example.myappmobile.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myappmobile.core.theme.*
import com.example.myappmobile.data.MockData
import com.example.myappmobile.domain.BannerData

@Composable
fun BannerSection(
    banner: BannerData,
    onCtaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .padding(horizontal = 20.dp)
            .clip(MaterialTheme.shapes.extraLarge),
    ) {
        AsyncImage(
            model = banner.imageUrl,
            contentDescription = banner.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.58f),
                        ),
                    ),
                ),
        )
        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp),
        ) {
            Text(
                text = banner.title,
                style = MaterialTheme.typography.displaySmall.copy(color = FloraWhite),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = banner.subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(color = FloraWhite.copy(alpha = 0.85f)),
            )
            Spacer(Modifier.height(20.dp))
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = Terracotta,
                modifier = Modifier.clickable(onClick = onCtaClick),
            ) {
                Text(
                    text = banner.ctaText,
                    style = MaterialTheme.typography.labelMedium.copy(color = FloraWhite),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8)
@Composable
fun BannerSectionPreview() {
    AtelierTheme {
        BannerSection(
            banner = MockData.banner,
            onCtaClick = {},
        )
    }
}
