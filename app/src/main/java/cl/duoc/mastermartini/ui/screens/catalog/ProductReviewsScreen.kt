package cl.duoc.mastermartini.ui.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mastermartini.data.repository.Review
import cl.duoc.mastermartini.data.repository.ReviewRepository
import cl.duoc.mastermartini.domain.ReviewHtml
import cl.duoc.mastermartini.ui.AppViewModelProvider
import cl.duoc.mastermartini.ui.components.CapsuleButton
import cl.duoc.mastermartini.ui.components.EmptyState
import cl.duoc.mastermartini.ui.components.MMCard
import cl.duoc.mastermartini.ui.components.PrimaryButton
import cl.duoc.mastermartini.ui.components.RichTextEditor
import cl.duoc.mastermartini.ui.components.RichTextEditorController
import java.util.Locale

private val STAR = Color(0xFFE0A100)

/** Opiniones de un producto (las mismas de la web) y formulario para dejar la propia. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductReviewsScreen(
    onBack: () -> Unit,
    vm: ProductReviewsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val editor = remember { RichTextEditorController() }
    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }
    // Vaciar el editor al publicar o cargar la opinión propia para editarla.
    LaunchedEffect(s.editorVersion) { if (s.editorVersion > 0) editor.setHtml(s.editorPreset) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Opiniones")
                        if (s.productName.isNotBlank()) {
                            Text(s.productName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when {
                s.loading && s.data == null -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                s.error != null && s.data == null -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    EmptyState(s.error.orEmpty())
                    CapsuleButton("Reintentar", onClick = vm::reload)
                }
                else -> {
                    val data = s.data
                    Summary(data?.average, data?.count ?: 0)
                    if (s.logged) WriteReview(s, editor, vm)
                    else Text("Inicia sesión para dejar tu opinión.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (data == null || data.reviews.isEmpty()) {
                        EmptyState("Aún no hay opiniones. ¡Sé el primero en contar tu experiencia!")
                    } else {
                        data.reviews.forEach { r -> ReviewCard(r, onEdit = vm::editMine, onDelete = { vm.askDelete(r) }) }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (s.confirmingDelete != null) {
        AlertDialog(
            onDismissRequest = vm::dismissDelete,
            title = { Text("¿Borrar tu opinión?") },
            text = { Text("Se quitará de la app y de la página web.") },
            confirmButton = { TextButton(onClick = vm::confirmDelete) { Text("Borrar") } },
            dismissButton = { TextButton(onClick = vm::dismissDelete) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun Summary(average: Double?, count: Long) {
    MMCard {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (average != null && count > 0) String.format(Locale("es", "CL"), "%.1f", average) else "–",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Stars((average ?: 0.0).let { kotlin.math.round(it).toInt() })
                Text(
                    when (count) { 0L -> "Sin opiniones"; 1L -> "1 opinión"; else -> "$count opiniones" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WriteReview(s: ReviewsUiState, editor: RichTextEditorController, vm: ProductReviewsViewModel) {
    MMCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (s.editingOwn) "Cambia tu opinión" else "Tu opinión", style = MaterialTheme.typography.titleMedium)
            if (s.editingOwn) {
                Text(
                    "Ya opinaste sobre este producto: al publicar se reemplaza tu opinión anterior.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                (1..5).forEach { n ->
                    IconButton(onClick = { vm.onRating(n) }, modifier = Modifier.size(44.dp)) {
                        Icon(
                            if (n <= s.rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            "$n de 5 estrellas",
                            tint = if (n <= s.rating) STAR else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
            }
            if (s.showErrors) s.ratingError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            RichTextEditor(
                controller = editor,
                onChange = vm::onEditorChange,
                isError = s.showErrors && s.textError != null,
            )
            Row {
                Text(
                    (if (s.showErrors) s.textError else null) ?: "Negrita, cursiva y listas. Sin enlaces.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (s.showErrors && s.textError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${s.textLength}/${ReviewRepository.MAX_CHARS}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (s.textLength > ReviewRepository.MAX_CHARS) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PrimaryButton(
                if (s.editingOwn) "Actualizar opinión" else "Publicar opinión",
                onClick = vm::publish,
                loading = s.sending,
            )
        }
    }
}

@Composable
private fun ReviewCard(review: Review, onEdit: () -> Unit, onDelete: () -> Unit) {
    val text = remember(review.html) { AnnotatedString.fromHtml(ReviewHtml.forDisplay(review.html)).trimEnd() }
    MMCard {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(review.author + if (review.mine) " (tú)" else "", style = MaterialTheme.typography.titleSmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Stars(review.rating, size = 16)
                        formatDate(review.date)?.let {
                            Spacer(Modifier.width(8.dp))
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (review.mine) {
                    IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, "Editar mi opinión") }
                    IconButton(onClick = onDelete) { Icon(Icons.Outlined.DeleteOutline, "Borrar mi opinión") }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun Stars(value: Int, size: Int = 20) {
    Row(Modifier.semantics { contentDescription = "$value de 5 estrellas" }) {
        (1..5).forEach { n ->
            Icon(
                if (n <= value) Icons.Filled.Star else Icons.Outlined.StarBorder,
                null,
                tint = if (n <= value) STAR else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(size.dp),
            )
        }
    }
}

private fun AnnotatedString.trimEnd(): AnnotatedString {
    var end = length
    while (end > 0 && this[end - 1].isWhitespace()) end--
    return subSequence(0, end)
}

/** "2026-11-26T15:04:05Z" → "26-11-2026". */
private fun formatDate(iso: String?): String? {
    val d = iso?.take(10)?.split("-") ?: return null
    return if (d.size == 3) "${d[2]}-${d[1]}-${d[0]}" else null
}
