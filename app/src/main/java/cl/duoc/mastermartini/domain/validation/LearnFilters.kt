package cl.duoc.mastermartini.domain.validation

import cl.duoc.mastermartini.data.model.LearnItem
import cl.duoc.mastermartini.data.model.LearnKind
import java.text.Normalizer

/** Selecciona únicamente categorías relevantes para la pestaña actual. */
object LearnFilters {
    fun categories(items: List<LearnItem>, kind: LearnKind): List<String> =
        items.asSequence().filter { it.kind == kind }.map { it.category }.distinct().sorted().toList()

    fun filter(
        items: List<LearnItem>,
        kind: LearnKind,
        category: String?,
        query: String,
        favorites: Set<String> = emptySet(),
        favoritesOnly: Boolean = false,
    ): List<LearnItem> {
        val term = normalize(query)
        return items.filter { item ->
            item.kind == kind &&
                (category == null || item.category == category) &&
                (!favoritesOnly || item.id in favorites) &&
                (term.isEmpty() || listOf(item.title, item.category, item.description)
                    .any { normalize(it).contains(term) })
        }
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
}
