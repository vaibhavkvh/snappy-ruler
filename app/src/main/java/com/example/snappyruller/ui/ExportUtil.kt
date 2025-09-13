package com.example.snappyruller.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.nativeCanvas
import java.io.File
import java.io.FileOutputStream

object ExportUtil {
    /**
     * Simple helper returning a Uri to a cache file path. In production you'd render the Compose Canvas to bitmap.
     * Here we create a placeholder PNG.
     */
    fun saveCanvasBitmapToCache(context: Context, filename: String): Uri? {
        return try {
            val bmp = Bitmap.createBitmap(800, 1200, Bitmap.Config.ARGB_8888)
            val file = File(context.cacheDir, filename)
            FileOutputStream(file).use { out ->
                bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}