package cl.duoc.mastermartini.device

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

/** Fotografía de perfil local, separada por ID de usuario; no se envía a servidores. */
object ProfilePhotoStore {
    private fun file(context: Context, userId: Long) = File(context.filesDir, "profile_$userId.img")

    fun saveFromGallery(context: Context, userId: Long, uri: Uri): Boolean = runCatching {
        val source = context.contentResolver.openInputStream(uri) ?: return@runCatching false
        val target = file(context, userId)
        source.use { input -> target.outputStream().use { input.copyTo(it) } }
        BitmapFactory.decodeFile(target.absolutePath) != null
    }.getOrDefault(false)

    fun saveFromCamera(context: Context, userId: Long, bitmap: Bitmap): Boolean = runCatching {
        file(context, userId).outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 87, output)
        }
    }.getOrDefault(false)

    fun read(context: Context, userId: Long): Bitmap? {
        val source = file(context, userId)
        if (!source.isFile) return null
        return BitmapFactory.decodeFile(source.absolutePath, BitmapFactory.Options().apply {
            inSampleSize = 2
        })
    }
}
