package cl.duoc.mastermartini

import cl.duoc.mastermartini.domain.ReviewHtml
import org.junit.Assert.assertEquals
import org.junit.Test

class ReviewHtmlTest {
    @Test
    fun convierteListasEnLineas() {
        val html = "<p><strong>Muy buena</strong></p><ul><li><p>Rinde</p></li><li><p>Sabe bien</p></li></ul>" +
            "<ol><li><p>Batir</p></li><li><p>Hornear</p></li></ol>"
        assertEquals(
            "<p><strong>Muy buena</strong></p><p>•&nbsp;Rinde<br>•&nbsp;Sabe bien</p><p>1.&nbsp;Batir<br>2.&nbsp;Hornear</p>",
            ReviewHtml.forDisplay(html),
        )
    }

    @Test
    fun textoSinListasQuedaIgual() {
        assertEquals("<p>Hola <em>mundo</em></p>", ReviewHtml.forDisplay("<p>Hola <em>mundo</em></p>"))
    }

    @Test
    fun textoPlanoCuentaCaracteres() {
        assertEquals("Rico & suave", ReviewHtml.plainText("<p>Rico &amp; <b>suave</b></p>"))
    }
}
