package cl.duoc.mastermartini.domain.validation

import cl.duoc.mastermartini.data.model.LearnKind
import cl.duoc.mastermartini.data.repository.FakeContentRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LearnFiltersTest {
    private val items = FakeContentRepository().learnItems()

    @Test fun `cada pestaña ofrece solamente sus categorias`() {
        val forRecipes = LearnFilters.categories(items, LearnKind.RECIPE)
        assertTrue("Heladería" in forRecipes)
        assertFalse("Galletas" in LearnFilters.categories(items, LearnKind.COURSE))
    }

    @Test fun `filtra por tipo y categoria`() {
        val result = LearnFilters.filter(items, LearnKind.RECIPE, "Heladería", "")
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.kind == LearnKind.RECIPE && it.category == "Heladería" })
    }

    @Test fun `busqueda ignora tildes y diferencias de mayusculas`() {
        val result = LearnFilters.filter(items, LearnKind.VIDEO, null, "CHOCOLATERIA")
        assertTrue(result.any { it.id == "v-templado" })
    }

    @Test fun `solo muestra favoritos realmente guardados`() {
        val result = LearnFilters.filter(items, LearnKind.RECIPE, null, "", setOf("r-croissant"), true)
        assertEquals(listOf("r-croissant"), result.map { it.id })
    }

    @Test fun `no muestra favoritos de otros tipos`() {
        val result = LearnFilters.filter(items, LearnKind.COURSE, null, "", setOf("r-croissant"), true)
        assertTrue(result.isEmpty())
    }

    @Test fun `cada contenido posee un identificador unico`() {
        assertEquals(items.size, items.map { it.id }.distinct().size)
    }
}
