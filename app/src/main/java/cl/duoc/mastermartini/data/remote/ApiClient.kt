package cl.duoc.mastermartini.data.remote

import com.google.gson.Gson
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Error de la API con un mensaje listo para mostrar al usuario. */
class ApiException(
    val status: Int,
    override val message: String,
    val fields: Map<String, String> = emptyMap(),
) : Exception(message)

/**
 * Cliente HTTP de la app.
 * - Agrega `Authorization: Bearer <token>` a cada solicitud.
 * - Si el backend responde 401 porque el token venció (dura 15 min), lo renueva una sola vez con el
 *   refresh token y repite la solicitud. Si la renovación falla, cierra la sesión.
 */
class ApiClient(
    val baseUrl: String,
    private val tokens: TokenStorage,
    private val onSessionExpired: () -> Unit = {},
) {
    private val gson = Gson()
    private val json = "application/json; charset=utf-8".toMediaType()
    private val lock = Any()

    private val base = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val autorizacion = Interceptor { chain ->
        val req = chain.request()
        val token = tokens.accessToken
        if (token == null || esRutaDeSesion(req)) chain.proceed(req)
        else chain.proceed(req.newBuilder().header("Authorization", "Bearer $token").build())
    }

    private val renovar = Authenticator { _: Route?, response: Response ->
        val req = response.request
        // Solo un reintento, y nunca para login/registro/refresh (ahí 401 = datos incorrectos).
        if (response.priorResponse != null || esRutaDeSesion(req)) return@Authenticator null
        val usado = req.header("Authorization")?.removePrefix("Bearer ")
        synchronized(lock) {
            val actual = tokens.accessToken
            // Otra solicitud ya renovó el token mientras esperábamos: lo reutilizamos.
            val nuevo = if (actual != null && actual != usado) actual else renovarToken()
            nuevo?.let { req.newBuilder().header("Authorization", "Bearer $it").build() }
        }
    }

    val http: OkHttpClient = base.newBuilder()
        .addInterceptor(autorizacion)
        .authenticator(renovar)
        .build()

    val api: MasterMartiniApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(http)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(MasterMartiniApi::class.java)

    /** Pide un access token nuevo con el refresh token guardado. Devuelve null si la sesión terminó. */
    fun renovarToken(): String? {
        val refresh = tokens.refreshToken() ?: return null
        val req = Request.Builder()
            .url(baseUrl + "api/v1/auth/refresh")
            .post(gson.toJson(RefreshBody(refresh)).toRequestBody(json))
            .build()
        return try {
            base.newCall(req).execute().use { res ->
                when {
                    res.isSuccessful -> {
                        val dto = gson.fromJson(res.body?.charStream(), TokenDto::class.java)
                        tokens.save(dto.accessToken, dto.refreshToken)
                        dto.accessToken
                    }
                    // 401: refresh vencido, revocado o reutilizado. Hay que volver a iniciar sesión.
                    res.code == 401 -> { tokens.clear(); onSessionExpired(); null }
                    else -> null
                }
            }
        } catch (_: IOException) {
            null // sin conexión: no cerramos la sesión, se reintentará después
        }
    }

    /** Ejecuta una llamada y convierte los errores HTTP y de red en [ApiException]. */
    suspend fun <T> llamar(bloque: suspend MasterMartiniApi.() -> T): T = try {
        api.bloque()
    } catch (e: HttpException) {
        val error = runCatching {
            gson.fromJson(e.response()?.errorBody()?.charStream(), ErrorDto::class.java)
        }.getOrNull()
        throw ApiException(
            e.code(),
            error?.message ?: mensajePorDefecto(e.code()),
            error?.fields.orEmpty().mapValues { it.value.removePrefix("Revisa este campo: ") },
        )
    } catch (e: IOException) {
        throw ApiException(0, "No se pudo conectar con el servidor. Revisa tu conexión e inténtalo nuevamente.")
    }

    /** Las imágenes pueden venir como URL completa o como ruta del sitio (/api/v1/imagenes/...). */
    fun urlImagen(ruta: String?): String? = when {
        ruta.isNullOrBlank() -> null
        // El emulador de Android no ve el "localhost" del PC: esas URLs se cambian por la del backend.
        LOCAL.containsMatchIn(ruta) -> ruta.replaceFirst(LOCAL, baseUrl)
        ruta.startsWith("http://") || ruta.startsWith("https://") -> ruta
        else -> baseUrl + ruta.trimStart('/')
    }

    private companion object {
        val LOCAL = Regex("""^https?://(localhost|127\.0\.0\.1)(:\d+)?/""")
    }

    private fun esRutaDeSesion(req: Request): Boolean {
        val path = req.url.encodedPath
        return path.endsWith("/auth/token") || path.endsWith("/auth/registro") || path.endsWith("/auth/refresh")
    }

    private fun mensajePorDefecto(code: Int) = when (code) {
        401 -> "Tu sesión terminó. Inicia sesión nuevamente."
        403 -> "No tienes permiso para esta acción."
        404 -> "No se encontró lo que buscabas."
        429 -> "Demasiados intentos. Espera unos minutos."
        in 500..599 -> "El servidor no está disponible en este momento. Inténtalo nuevamente."
        else -> "No se pudo completar la solicitud."
    }
}
