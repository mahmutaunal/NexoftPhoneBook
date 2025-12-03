package com.mahmutalperenunal.nexoftphonebook.presentation.ui

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import java.io.File

val LocalAppImageLoader = staticCompositionLocalOf<ImageLoader> {
    error("No ImageLoader provided")
}

@Composable
fun ProvideAppImageLoader(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val imageLoader = remember {
        buildAppImageLoader(context)
    }

    CompositionLocalProvider(
        LocalAppImageLoader provides imageLoader,
        content = content
    )
}

private fun buildAppImageLoader(context: Context): ImageLoader {
    val cacheDir = File(context.cacheDir, "coil_cache")

    return ImageLoader.Builder(context)
        .crossfade(true)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir)
                .maxSizeBytes(50L * 1024 * 1024)
                .build()
        }
        .respectCacheHeaders(false)
        .build()
}