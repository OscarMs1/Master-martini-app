package cl.duoc.mastermartini.data.remote

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Dónde guarda la app los tokens de la sesión. Es una interfaz para poder probarla sin Android. */
interface TokenStorage {
    /** Token de acceso (JWT, 15 min). Solo en memoria: se pierde al cerrar la app. */
    var accessToken: String?
    fun refreshToken(): String?
    fun save(accessToken: String, refreshToken: String?)
    fun clear()
}

/**
 * El refresh token (7 días) se guarda cifrado con una llave AES del Android Keystore: la llave
 * nunca sale del hardware seguro del teléfono, así que copiar el archivo de preferencias no sirve
 * para robar la sesión. El access token vive solo en memoria.
 */
class TokenStore(context: Context) : TokenStorage {
    private val prefs = context.getSharedPreferences("mm_sesion", Context.MODE_PRIVATE)

    @Volatile override var accessToken: String? = null

    override fun refreshToken(): String? {
        val guardado = prefs.getString(KEY, null) ?: return null
        return runCatching { descifrar(guardado) }.getOrElse {
            // Llave borrada (por ejemplo, al restaurar el teléfono): la sesión no se puede recuperar.
            prefs.edit().remove(KEY).apply()
            null
        }
    }

    override fun save(accessToken: String, refreshToken: String?) {
        this.accessToken = accessToken
        if (refreshToken != null) prefs.edit().putString(KEY, cifrar(refreshToken)).apply()
    }

    override fun clear() {
        accessToken = null
        prefs.edit().remove(KEY).apply()
    }

    private fun llave(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return gen.generateKey()
    }

    private fun cifrar(texto: String): String {
        val cipher = Cipher.getInstance(TRANSFORMACION).apply { init(Cipher.ENCRYPT_MODE, llave()) }
        val datos = cipher.iv + cipher.doFinal(texto.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(datos, Base64.NO_WRAP)
    }

    private fun descifrar(guardado: String): String {
        val datos = Base64.decode(guardado, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMACION)
        cipher.init(Cipher.DECRYPT_MODE, llave(), GCMParameterSpec(128, datos, 0, IV))
        return String(cipher.doFinal(datos, IV, datos.size - IV), Charsets.UTF_8)
    }

    private companion object {
        const val KEY = "refresh"
        const val ALIAS = "mm_refresh_token"
        const val TRANSFORMACION = "AES/GCM/NoPadding"
        const val IV = 12
    }
}

/** Implementación en memoria para los tests. */
class MemoryTokenStorage : TokenStorage {
    @Volatile override var accessToken: String? = null
    @Volatile private var refresh: String? = null
    override fun refreshToken() = refresh
    override fun save(accessToken: String, refreshToken: String?) {
        this.accessToken = accessToken
        if (refreshToken != null) refresh = refreshToken
    }
    override fun clear() { accessToken = null; refresh = null }
}
