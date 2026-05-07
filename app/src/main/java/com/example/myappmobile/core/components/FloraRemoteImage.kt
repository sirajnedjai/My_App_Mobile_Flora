package com.example.myappmobile.core.components

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myappmobile.R
import com.example.myappmobile.data.remote.ImageUrlResolver

@Composable
fun FloraRemoteImage(
    imageUrl: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val fallbackPainter = safePainterResourceOrNull(R.drawable.image_placeholder)
    val model: Any? = when (imageUrl) {
        is Int -> imageUrl
        is String -> ImageUrlResolver.resolveOrNull(imageUrl)
        else -> null
    }
    val request = model?.let { source ->
        ImageRequest.Builder(context)
            .data(source)
            .crossfade(true)
            .build()
    }

    AsyncImage(
        model = request ?: model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = fallbackPainter,
        error = fallbackPainter,
        fallback = fallbackPainter,
    )
}

@Composable
fun safePainterResourceOrNull(resId: Int): Painter? {
    val context = LocalContext.current
    val isSupportedPainterResource = remember(resId) {
        runCatching {
            when (val drawable = context.getDrawable(resId)) {
                is BitmapDrawable,
                is VectorDrawable -> true
                else -> false
            }
        }.getOrDefault(false)
    }

    return if (isSupportedPainterResource) painterResource(id = resId) else null
}
