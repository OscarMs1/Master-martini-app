package cl.duoc.mastermartini.data.repository

import cl.duoc.mastermartini.data.remote.ApiClient
import cl.duoc.mastermartini.data.remote.ApiException
import cl.duoc.mastermartini.data.remote.OpinionBody

/** Una opinión de un cliente. [html] trae formato básico (negrita, cursiva, listas). */
data class Review(
    val id: Long,
    val author: String,
    val rating: Int,
    val html: String,
    val date: String?,
    val mine: Boolean,
)

data class ProductReviews(
    val average: Double?,
    val count: Long,
    val reviews: List<Review>,
) {
    val mine: Review? get() = reviews.firstOrNull { it.mine }
}

/**
 * Reseñas de productos (/api/v1/resenas). Son las mismas que se ven en la ficha del producto en la
 * web: cada cliente tiene una opinión por producto, y al publicar de nuevo se reemplaza.
 */
class ReviewRepository(private val client: ApiClient) {

    /** Lanza [ApiException] si el producto no existe en el servidor o no hay conexión. */
    suspend fun load(productId: String): ProductReviews {
        val dto = client.llamar { resenas(remoteId(productId)) }
        val list = dto.opiniones.orEmpty().map {
            Review(
                id = it.id,
                author = it.autor?.takeIf(String::isNotBlank) ?: "Cliente",
                rating = it.puntuacion.coerceIn(1, 5),
                html = it.comentario.orEmpty(),
                date = it.fecha,
                mine = it.mia == true,
            )
        }
        return ProductReviews(dto.promedio, dto.cantidad ?: list.size.toLong(), list)
    }

    suspend fun publish(productId: String, rating: Int, html: String) {
        client.llamar { opinar(remoteId(productId), OpinionBody(rating, html)) }
    }

    suspend fun delete(reviewId: Long) {
        client.llamar { borrarResena(reviewId) }
    }

    private fun remoteId(productId: String): Long =
        productId.toLongOrNull() ?: throw ApiException(404, "Este producto no tiene opiniones.")

    companion object {
        const val MIN_CHARS = 10
        const val MAX_CHARS = 500
    }
}
