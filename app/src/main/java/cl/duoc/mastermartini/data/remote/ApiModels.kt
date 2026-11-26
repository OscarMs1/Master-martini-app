package cl.duoc.mastermartini.data.remote

/**
 * Objetos que viajan en JSON entre la app y el backend (Spring Boot, rutas /api/v1).
 * Los nombres coinciden con los del backend; Gson los convierte automáticamente.
 */

// ---------- Cuentas ----------

data class LoginBody(val email: String, val password: String)

data class RegistroBody(
    val nombre: String,
    val email: String,
    val password: String,
    val run: String,
    val telefono: String,
    val region: String,
    val comuna: String,
    val direccion: String,
)

data class RefreshBody(val refreshToken: String)

data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val email: String,
    val rol: String,
    val run: String?,
    val telefono: String?,
    val region: String?,
    val comuna: String?,
    val direccion: String?,
)

data class TokenDto(
    val accessToken: String,
    val tokenType: String?,
    val expiresIn: Long,
    val refreshToken: String?,
    val usuario: UsuarioDto,
)

// ---------- Catálogo ----------

data class ProductoDto(
    val id: Long,
    val nombre: String,
    val categoria: String,
    val formato: String,
    val descripcion: String,
    val precio: Long,
    val descuento: Int,
    val stock: Int,
    val imagen: String,
    val destacado: Boolean,
    val retirado: Boolean? = null,
)

data class ProductoBody(
    val nombre: String,
    val categoria: String,
    val formato: String,
    val descripcion: String,
    val precio: Long,
    val descuento: Int,
    val stock: Int,
    val imagen: String,
    val destacado: Boolean,
)

data class ImagenSubidaDto(val url: String)

// ---------- Pedidos ----------

data class PedidoBody(
    val requestKey: String,
    val nombre: String,
    val email: String,
    val run: String,
    val telefono: String,
    val region: String,
    val comuna: String,
    val direccion: String,
    /** "domicilio" o "retiro" */
    val entrega: String,
    /** Pago simulado: "APROBADO" o "RECHAZADO" (proyecto académico, no hay cobro real). */
    val pago: String,
    val items: List<PedidoItemBody>,
)

data class PedidoItemBody(val id: Long, val cantidad: Int)

data class PedidoLineaDto(val productoId: Long, val nombre: String, val cantidad: Int, val precioUnitario: Long?)

data class PedidoDto(
    val id: Long,
    val fecha: String,
    val comuna: String?,
    val direccion: String?,
    val entrega: String?,
    val estado: String,
    val subtotal: Long,
    val descuento: Long,
    val despacho: Long,
    val total: Long,
    val lineas: List<PedidoLineaDto>?,
)

// ---------- Clases e inscripciones ----------

data class ClaseDto(
    val id: Long,
    val titulo: String,
    val descripcion: String?,
    val fecha: String,
    val modalidad: String?,
    val nivel: String?,
    val duracion: String?,
    val lugar: String?,
    val cuposDisponibles: Int,
    val imagen: String? = null,
    /** Productos del catálogo que se usan en la clase (sirve para recomendar clases según el carrito). */
    val productos: List<ClaseProductoDto>? = null,
)

data class ClaseProductoDto(val id: Long, val nombre: String?)

data class InscripcionBody(val claseId: Long)

data class InscripcionDto(
    val id: Long,
    val claseId: Long,
    val titulo: String,
    val modalidad: String?,
    val fechaClase: String?,
)

// ---------- Direcciones guardadas y favoritos (cuenta con sesión iniciada) ----------

data class DireccionBody(
    val etiqueta: String,
    val nombre: String,
    val email: String,
    val run: String,
    val telefono: String,
    val region: String,
    val comuna: String,
    val direccion: String,
)

data class DireccionDto(
    val id: Long,
    val etiqueta: String?,
    val nombre: String?,
    val telefono: String?,
    val region: String?,
    val comuna: String?,
    val direccion: String?,
)

data class FavoritoDto(val productoId: Long, val fecha: String?)

// Reseñas de productos. El comentario es HTML con formato básico (negrita, cursiva, listas):
// el backend lo limpia antes de guardarlo.
data class ResenasDto(
    val productoId: Long,
    val promedio: Double?,
    val cantidad: Long?,
    val opiniones: List<ResenaDto>?,
)

data class ResenaDto(
    val id: Long,
    val autor: String?,
    val puntuacion: Int,
    val comentario: String?,
    val fecha: String?,
    val mia: Boolean?,
)

data class OpinionBody(val puntuacion: Int, val comentario: String)

// ---------- Contacto (solicitudes de asesoría) ----------

data class ContactoBody(val nombre: String, val email: String, val asunto: String, val mensaje: String)

/** Error estándar del backend: {"message": "...", "fields": {"campo": "mensaje"}} */
data class ErrorDto(val message: String?, val fields: Map<String, String>?)
