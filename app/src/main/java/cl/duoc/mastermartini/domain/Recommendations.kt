package cl.duoc.mastermartini.domain

import cl.duoc.mastermartini.data.model.Product
import cl.duoc.mastermartini.data.model.ServiceItem

/** Producto sugerido en el carrito, con el motivo que se muestra al usuario. */
data class ProductSuggestion(val product: Product, val reason: String)

/** Clase sugerida según lo que hay en el carrito. */
data class ClassSuggestion(val service: ServiceItem, val reason: String)

/**
 * Recomendaciones del carrito. Se calculan en la app con el catálogo y las clases del backend
 * (sin historial de otros clientes):
 * - Productos que se usan junto a los del carrito en alguna clase pesan más.
 * - Luego, productos de la misma línea (categoría), destacados y con descuento.
 * - Nunca se sugiere algo que ya está en el carrito o sin stock.
 */
object Recommendations {

    fun products(
        cartIds: Set<String>,
        catalog: List<Product>,
        classes: List<ServiceItem>,
        limit: Int = 6,
    ): List<ProductSuggestion> {
        val inCart = catalog.filter { it.id in cartIds }
        val candidates = catalog.filter { it.id !in cartIds && it.stock > 0 }
        if (inCart.isEmpty()) {
            // Carrito vacío: mostramos los destacados y ofertas.
            return candidates.filter { it.featured || it.discount > 0 }
                .sortedWith(compareByDescending<Product> { it.featured }.thenByDescending { it.discount })
                .take(limit)
                .map { ProductSuggestion(it, if (it.discount > 0) "Oferta: -${it.discount} %" else "Destacado") }
        }
        val categories = inCart.map { it.category }.toSet()
        // Producto sugerido -> producto del carrito con el que comparte una clase.
        val usedTogether = mutableMapOf<String, String>()
        for (c in classes) {
            val shared = c.productIds.firstOrNull { it in cartIds } ?: continue
            val cartName = inCart.firstOrNull { it.id == shared }?.name ?: continue
            c.productIds.filter { it !in cartIds }.forEach { usedTogether.putIfAbsent(it, cartName) }
        }
        return candidates
            .map { p ->
                var score = 0
                if (p.id in usedTogether) score += 4
                if (p.category in categories) score += 2
                if (p.featured) score += 1
                if (p.discount > 0) score += 1
                p to score
            }
            .filter { (_, score) -> score >= 2 }
            .sortedWith(compareByDescending<Pair<Product, Int>> { it.second }.thenBy { it.first.name })
            .take(limit)
            .map { (p, _) ->
                val reason = when {
                    p.id in usedTogether -> "Se usa junto a ${usedTogether[p.id]}"
                    p.category in categories -> "Más de ${p.category}"
                    else -> "Destacado"
                }
                ProductSuggestion(p, reason)
            }
    }

    fun classes(
        cartIds: Set<String>,
        catalog: List<Product>,
        classes: List<ServiceItem>,
        enrolledIds: Set<String> = emptySet(),
        limit: Int = 3,
    ): List<ClassSuggestion> {
        if (cartIds.isEmpty()) return emptyList()
        val byId = catalog.associateBy { it.id }
        val cartCategories = cartIds.mapNotNull { byId[it]?.category }.toSet()
        return classes
            .filter { it.id !in enrolledIds && it.availableSeats > 0 }
            .mapNotNull { c ->
                val direct = c.productIds.filter { it in cartIds }
                val sameLine = c.productIds.mapNotNull { byId[it]?.category }.firstOrNull { it in cartCategories }
                when {
                    direct.isNotEmpty() -> Triple(c, 2 + direct.size, "Usa ${byId[direct.first()]?.name ?: "un producto"} de tu carrito")
                    sameLine != null -> Triple(c, 1, "Ideal para tu línea de $sameLine")
                    else -> null
                }
            }
            .sortedByDescending { it.second }
            .take(limit)
            .map { (c, _, reason) -> ClassSuggestion(c, reason) }
    }
}
