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

    // Direcciones guardadas (máximo 5 por cuenta, las mismas de la web)
    @GET("api/v1/direcciones")
    suspend fun direcciones(): List<DireccionDto>

    @POST("api/v1/direcciones")
    suspend fun guardarDireccion(@Body body: DireccionBody)

    @DELETE("api/v1/direcciones/{id}")
    suspend fun borrarDireccion(@Path("id") id: Long)

    // Favoritos
    @GET("api/v1/favoritos")
    suspend fun favoritos(): List<FavoritoDto>

    @POST("api/v1/favoritos/{id}")
    suspend fun agregarFavorito(@Path("id") productoId: Long)

    @DELETE("api/v1/favoritos/{id}")
    suspend fun quitarFavorito(@Path("id") productoId: Long)

    // Reseñas de productos (las mismas de la ficha en la web). Una por cliente y producto.
    @GET("api/v1/resenas/producto/{id}")
    suspend fun resenas(@Path("id") productoId: Long): ResenasDto

    @POST("api/v1/resenas/producto/{id}")
    suspend fun opinar(@Path("id") productoId: Long, @Body body: OpinionBody)

    @DELETE("api/v1/resenas/{id}")
    suspend fun borrarResena(@Path("id") id: Long)

    // Contacto
    @POST("api/v1/contactos")
    suspend fun contacto(@Body body: ContactoBody)
}
