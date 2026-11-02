package cl.duoc.mastermartini.device

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.util.UUID

/** Conserva una copia privada de cada imagen escogida con el selector nativo de Android. */
object CatalogPhotoStore {
    fun save(context: Context, uri: Uri): String? {
        return try {
        val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            ?: return null
        val size = 960f / maxOf(bitmap.width, bitmap.height).coerceAtLeast(1)
        val output = if (size >= 1f) bitmap else Bitmap.createScaledBitmap(
            bitmap, (bitmap.width * size).toInt().coerceAtLeast(1),
            (bitmap.height * size).toInt().coerceAtLeast(1), true,
        )
        val folder = File(context.filesDir, "catalog_photos").also { it.mkdirs() }
        val file = File(folder, "${UUID.randomUUID()}.jpg")
        val saved = file.outputStream().use { output.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        if (output !== bitmap) output.recycle()
        bitmap.recycle()
        if (saved) file.absolutePath else null
        } catch (_: Exception) { null }
    }

    fun read(context: Context, path: String?): Bitmap? {
        if (path.isNullOrBlank()) return null
        return runCatching {
            val base = File(context.filesDir, "catalog_photos").canonicalFile
            val target = File(path).canonicalFile
            if (target.parentFile != base) null else BitmapFactory.decodeFile(target.path)
        }.getOrNull()
    }
}
