package cl.duoc.mastermartini.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject

/** Formato activo donde está el cursor (para marcar los botones de la barra). */
data class EditorMarks(
    val bold: Boolean = false,
    val italic: Boolean = false,
    val bulletList: Boolean = false,
    val orderedList: Boolean = false,
)

/** Permite a la pantalla vaciar el editor o cargar un texto (por ejemplo, al editar una reseña). */
class RichTextEditorController {
    internal var webView: WebView? = null
    internal var ready = false
    private var pendingHtml: String? = null

    fun setHtml(html: String) {
        if (ready) run("window.mmEditor.ponerHtml(${JSONObject.quote(html)})") else pendingHtml = html
    }

    internal fun command(name: String) = run("window.mmEditor.comando('$name')")

    internal fun onReady() {
        ready = true
        pendingHtml?.let { pendingHtml = null; setHtml(it) }
    }

    private fun run(js: String) {
        webView?.evaluateJavascript("if (window.mmEditor) { $js }", null)
    }
}

/**
 * Editor de texto con formato: es TipTap, el mismo editor de la página web, empaquetado en
 * `assets/tiptap` (funciona sin internet). Entrega el HTML y el largo del texto visible.
 * Formato permitido: negrita, cursiva y listas (igual que las reseñas en la web).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RichTextEditor(
    controller: RichTextEditorController,
    onChange: (html: String, textLength: Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 150.dp,
    isError: Boolean = false,
) {
    val latestOnChange by rememberUpdatedState(onChange)
    var marks by remember { mutableStateOf(EditorMarks()) }
    val textColor = MaterialTheme.colorScheme.onSurface.toCss()
    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant.toCss()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (isError) 2.dp else 1.dp,
            if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Column {
            Row(Modifier.padding(horizontal = 4.dp)) {
                FormatButton(Icons.Filled.FormatBold, "Negrita", marks.bold) { controller.command("bold") }
                FormatButton(Icons.Filled.FormatItalic, "Cursiva", marks.italic) { controller.command("italic") }
                FormatButton(Icons.AutoMirrored.Filled.FormatListBulleted, "Lista con viñetas", marks.bulletList) { controller.command("bulletList") }
                FormatButton(Icons.Filled.FormatListNumbered, "Lista numerada", marks.orderedList) { controller.command("orderedList") }
                IconButton(onClick = { controller.command("undo") }) { Icon(Icons.AutoMirrored.Filled.Undo, "Deshacer") }
                IconButton(onClick = { controller.command("redo") }) { Icon(Icons.AutoMirrored.Filled.Redo, "Rehacer") }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(height),
                factory = { context ->
                    WebView(context).apply {
                        setBackgroundColor(AndroidColor.TRANSPARENT)
                        settings.javaScriptEnabled = true
                        settings.allowContentAccess = false
                        addJavascriptInterface(object {
                            // Llega desde el hilo del WebView: se pasa al hilo principal.
                            @JavascriptInterface
                            fun onChange(html: String, textLength: Int, state: String) {
                                post {
                                    latestOnChange(html, textLength)
                                    marks = runCatching {
                                        val o = JSONObject(state)
                                        EditorMarks(o.optBoolean("bold"), o.optBoolean("italic"), o.optBoolean("bulletList"), o.optBoolean("orderedList"))
                                    }.getOrDefault(EditorMarks())
                                }
                            }
                        }, "MMAndroid")
                        webViewClient = object : WebViewClient() {
                            // El editor nunca abre otras páginas.
                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = true
                            override fun onPageFinished(view: WebView, url: String) {
                                view.evaluateJavascript(
                                    "document.body.style.color='$textColor';" +
                                        "document.documentElement.style.setProperty('--hint','$hintColor');",
                                    null,
                                )
                                controller.onReady()
                            }
                        }
                        controller.webView = this
                        loadUrl("file:///android_asset/tiptap/editor.html")
                    }
                },
                onRelease = { view ->
                    controller.webView = null
                    controller.ready = false
                    view.destroy()
                },
            )
        }
    }
}

@Composable
private fun FormatButton(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    IconToggleButton(checked = active, onCheckedChange = { onClick() }) {
        Icon(icon, label, tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun Color.toCss(): String = String.format("#%06X", 0xFFFFFF and toArgb())
