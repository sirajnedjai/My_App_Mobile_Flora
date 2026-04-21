package com.example.myappmobile.core.components

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.myappmobile.R

@Composable
fun FloraRemoteImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val resolvedModel = imageUrl?.trim()?.takeIf { it.isNotBlank() }
    val fallbackPainter = painterResource(R.drawable.flora_logo_vectorized)
    val imageRequest = resolvedModel?.let { model ->
        ImageRequest.Builder(context)
            .data(model)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .crossfade(true)
            .build()
    }

    LaunchedEffect(resolvedModel, contentDescription) {
        Log.d(TAG, "Rendering image. description=$contentDescription finalUrl=${resolvedModel.orEmpty()}")
    }

    AsyncImage(
        model = imageRequest ?: resolvedModel,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        fallback = fallbackPainter,
        error = fallbackPainter,
        placeholder = fallbackPainter,
    )
}

private const val TAG = "FloraRemoteImage"
