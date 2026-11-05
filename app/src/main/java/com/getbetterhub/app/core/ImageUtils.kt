package com.getbetterhub.app.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Helpers for the profile picture and banner: copy a picked image into private storage and show it. */
object ImageUtils {
    private const val TAG = "ImageUtils"

    /** Copies the picked image into the app's private files dir so it survives even if the gallery item moves. */
    fun copyToPrivateStorage(context: Context, uri: Uri, prefix: String): String? = try {
        // Remove older copies of the same kind so storage doesn't grow every time the user changes it.
        context.filesDir.listFiles()?.filter { it.name.startsWith(prefix) }?.forEach { it.delete() }
        val out = File(context.filesDir, "${prefix}_${System.currentTimeMillis()}.jpg")
        val input = context.contentResolver.openInputStream(uri)
        if (input == null) {
            null
        } else {
            input.use { src -> out.outputStream().use { dst -> src.copyTo(dst) } }
            out.absolutePath
        }
    } catch (e: Exception) {
        AppLog.e(TAG, "could not copy picked image", e)
        null
    }

    fun delete(path: String?) {
        if (path != null) File(path).delete()
    }

    /** Decodes a downsampled bitmap so large camera photos don't blow up memory. */
    fun decodeSampled(path: String, maxPx: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0) return null
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxPx) sample *= 2
        return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    }
}

@Composable
fun rememberFileImage(path: String?, maxPx: Int = 1080): ImageBitmap? {
    val state = produceState<ImageBitmap?>(initialValue = null, path) {
        value = if (path == null) null else withContext(Dispatchers.IO) { ImageUtils.decodeSampled(path, maxPx)?.asImageBitmap() }
    }
    return state.value
}
