package com.mahmutalperenunal.nexoftphonebook.presentation.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream


// Reads an image from a Uri, optionally downscales it, and returns compressed JPEG bytes
fun readBytesFromUri(
    context: Context,
    uri: Uri,
    maxDimension: Int = 720,
    quality: Int = 75
): ByteArray? {
    return try {
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        }

        val srcWidth = boundsOptions.outWidth
        val srcHeight = boundsOptions.outHeight
        if (srcWidth <= 0 || srcHeight <= 0) return null

        val larger = maxOf(srcWidth, srcHeight)
        var inSampleSize = 1
        if (larger > maxDimension) {
            inSampleSize = larger / maxDimension
            if (inSampleSize < 1) inSampleSize = 1
        }

        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
        }

        val scaledBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: return null

        val output = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
        scaledBitmap.recycle()

        output.toByteArray()
    } catch (_: Exception) {
        null
    }
}