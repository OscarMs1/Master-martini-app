package cl.duoc.mastermartini.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Rutas del backend que usa la app. Es la misma API que usa la página web (documentada en
 * Swagger: /swagger-ui.html). El token JWT lo agrega [ApiClient] automáticamente.
 */
interface MasterMartiniApi {
    // Cuentas. La app usa /auth/token (no /auth/login): recibe el refreshToken en el JSON,
    // porque no usa cookies como la web.
    @POST("api/v1/auth/token")
    suspend fun login(@Body body: LoginBody): TokenDto

    @POST("api/v1/auth/registro")
    suspend fun registro(@Body body: RegistroBody): UsuarioDto

    @GET("api/v1/auth/me")
    suspend fun yo(): UsuarioDto

    @POST("api/v1/auth/logout")
    suspend fun logout(@Body body: RefreshBody)

    // Catálogo
    @GET("api/v1/productos")
    suspend fun productos(): List<ProductoDto>

    @POST("api/v1/productos")
    suspend fun crearProducto(@Body body: ProductoBody): ProductoDto

    @PUT("api/v1/productos/{id}")
    suspend fun editarProducto(@Path("id") id: Long, @Body body: ProductoBody): ProductoDto

    @DELETE("api/v1/productos/{id}")
    suspend fun eliminarProducto(@Path("id") id: Long)

    @Multipart
    @POST("api/v1/admin/imagenes")
    suspend fun subirImagen(@Part archivo: MultipartBody.Part): ImagenSubidaDto

    // Pedidos
    @POST("api/v1/pedidos")
    suspend fun crearPedido(@Body body: PedidoBody): PedidoDto

    @GET("api/v1/pedidos/mis-pedidos")
    suspend fun misPedidos(): List<PedidoDto>

    // Clases
    @GET("api/v1/clases")
    suspend fun clases(): List<ClaseDto>

    @POST("api/v1/inscripciones")
    suspend fun inscribir(@Body body: InscripcionBody): InscripcionDto

    @GET("api/v1/inscripciones/mis-inscripciones")
    suspend fun misInscripciones(): List<InscripcionDto>

    // Contacto
    @POST("api/v1/contactos")
    suspend fun contacto(@Body body: ContactoBody)
}
