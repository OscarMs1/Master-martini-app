package cl.duoc.mastermartini.device

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

/** Prepara la foto elegida con el selector de Android para subirla al backend. */
object CatalogPhotoStore {
    /** Reduce la imagen a 960 px como máximo y la convierte a JPEG. Devuelve null si no se pudo leer. */
    fun toJpeg(context: Context, uri: Uri): ByteArray? = try {
        val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        if (bitmap == null) null else {
            val size = 960f / maxOf(bitmap.width, bitmap.height).coerceAtLeast(1)
            val output = if (size >= 1f) bitmap else Bitmap.createScaledBitmap(
                bitmap, (bitmap.width * size).toInt().coerceAtLeast(1),
                (bitmap.height * size).toInt().coerceAtLeast(1), true,
            )
            val bytes = ByteArrayOutputStream().also { output.compress(Bitmap.CompressFormat.JPEG, 85, it) }.toByteArray()
            if (output !== bitmap) output.recycle()
            bitmap.recycle()
            bytes
        }
    } catch (_: Exception) {
        null
    }
}
