package cl.duoc.mastermartini.domain

/**
 * Prepara el HTML de una reseña para mostrarlo con `AnnotatedString.fromHtml`, que no dibuja
 * listas: cada `<li>` pasa a ser una línea con "•" o con su número ("1.", "2.", …).
 * El HTML ya viene limpio desde el backend (solo p, br, strong, em, u, s, ul, ol, li).
 */
object ReviewHtml {
    private val TAG = Regex("<(/?)(ul|ol|li|p)\\b[^>]*>", RegexOption.IGNORE_CASE)

    fun forDisplay(html: String): String {
        val out = StringBuilder()
        // Por cada lista abierta: si es numerada y en qué número va.
        val lists = ArrayDeque<IntArray>()
        var firstItem = true
        var last = 0
        for (m in TAG.findAll(html)) {
            out.append(html, last, m.range.first)
            last = m.range.last + 1
            val closing = m.groupValues[1] == "/"
            when (m.groupValues[2].lowercase()) {
                // Dentro de una lista, TipTap envuelve cada punto en <p>: se omite para no dejar líneas en blanco.
                "p" -> if (lists.isEmpty()) out.append(if (closing) "</p>" else "<p>")
                "ul", "ol" -> if (closing) {
                    lists.removeLastOrNull()
                    if (lists.isEmpty()) out.append("</p>")
                } else {
                    if (lists.isEmpty()) { out.append("<p>"); firstItem = true }
                    lists.addLast(intArrayOf(if (m.groupValues[2].equals("ol", true)) 1 else 0, 0))
                }
                "li" -> if (!closing && lists.isNotEmpty()) {
                    if (!firstItem) out.append("<br>")
                    firstItem = false
                    repeat(lists.size - 1) { out.append("&nbsp;&nbsp;&nbsp;&nbsp;") }
                    val list = lists.last()
                    if (list[0] == 1) { list[1]++; out.append(list[1]).append(".&nbsp;") } else out.append("•&nbsp;")
                }
            }
        }
        out.append(html, last, html.length)
        return out.toString()
    }

    /** Texto plano aproximado (sin etiquetas), para contar caracteres o leer en voz alta. */
    fun plainText(html: String): String =
        html.replace(Regex("<[^>]*>"), " ")
            .replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&amp;", "&")
            .replace(Regex("\\s+"), " ").trim()
}
