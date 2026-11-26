package cl.duoc.mastermartini

import cl.duoc.mastermartini.data.model.Product
import cl.duoc.mastermartini.data.model.ServiceItem
import cl.duoc.mastermartini.data.model.ServiceType
import cl.duoc.mastermartini.domain.Recommendations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationsTest {
    private fun p(id: String, cat: String, stock: Int = 10, featured: Boolean = false, discount: Int = 0) =
        Product(id, "Producto $id", cat, listOf("1 kg"), "", price = 1000, discount = discount, stock = stock, featured = featured)

    private fun clase(id: String, vararg products: String, seats: Int = 10) =
        ServiceItem("clase-$id", ServiceType.EVENT, "Clase $id", "PRESENCIAL", "", totalSeats = seats, productIds = products.toList())

    private val catalog = listOf(
        p("1", "Panadería"), p("2", "Pastelería"), p("3", "Pastelería"),
        p("4", "Gelatería"), p("6", "Gelatería", stock = 0), p("7", "Retail", featured = true),
    )
    private val classes = listOf(clase("a", "2", "3"), clase("b", "4", "6"), clase("c", "1", seats = 0))

    @Test fun usedTogetherInAClassRanksFirst() {
        val s = Recommendations.products(setOf("2"), catalog, classes)
        assertEquals("3", s.first().product.id)
        assertTrue(s.first().reason.contains("Producto 2"))
    }

    @Test fun neverSuggestsCartItemsOrOutOfStock() {
        val ids = Recommendations.products(setOf("4"), catalog, classes).map { it.product.id }
        assertFalse("4" in ids)
        assertFalse("6" in ids)
    }

    @Test fun emptyCartShowsFeatured() {
        val s = Recommendations.products(emptySet(), catalog, classes)
        assertEquals(listOf("7"), s.map { it.product.id })
    }

    @Test fun classesMatchCartProductsAndSkipFullOrEnrolled() {
        assertEquals(listOf("clase-a"), Recommendations.classes(setOf("2"), catalog, classes).map { it.service.id })
        // La clase c usa el producto 1 pero no tiene cupos.
        assertTrue(Recommendations.classes(setOf("1"), catalog, classes).isEmpty())
        assertTrue(Recommendations.classes(setOf("2"), catalog, classes, enrolledIds = setOf("clase-a")).isEmpty())
        assertTrue(Recommendations.classes(emptySet(), catalog, classes).isEmpty())
    }
}
