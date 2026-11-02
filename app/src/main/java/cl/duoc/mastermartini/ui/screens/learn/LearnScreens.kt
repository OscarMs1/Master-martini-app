package cl.duoc.mastermartini.ui.screens.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import cl.duoc.mastermartini.ui.components.FoodPhoto
import androidx.compose.ui.draw.clip
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cl.duoc.mastermartini.ui.theme.LocalExtraColors
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mastermartini.data.model.LearnItem
import cl.duoc.mastermartini.data.model.LearnKind
import cl.duoc.mastermartini.device.ContactHelper
import cl.duoc.mastermartini.ui.AppViewModelProvider
import cl.duoc.mastermartini.ui.components.EmptyState
import cl.duoc.mastermartini.ui.components.MMCard
import cl.duoc.mastermartini.ui.components.Pill
import cl.duoc.mastermartini.ui.components.ScreenTitle
import kotlinx.coroutines.launch

@Composable
fun LearnScreen(
    onOpenContent: (String) -> Unit,
    vm: LearnViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val s by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Aprende", Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
        SearchField(s.query, vm::onQuery, "Buscar en la biblioteca", Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(12.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            LearnKind.entries.forEachIndexed { i, kind ->
                SegmentedButton(
                    selected = s.kind == kind,
                    onClick = { vm.onKind(kind) },
                    shape = SegmentedButtonDefaults.itemShape(i, LearnKind.entries.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) { Text(kind.label) }
            }
        }
        Spacer(Modifier.height(12.dp))
        CategoryChips(s.categories, s.category, vm::onCategory)
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${s.items.size} ${if (s.items.size == 1) "contenido" else "contenidos"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = vm::toggleFavorites) {
                Text(if (s.favoritesOnly) "Mostrar todos" else "Mis favoritos")
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (s.items.isEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        EmptyState(if (s.favoritesOnly) "Aún no tienes favoritos en esta sección." else "No hay coincidencias para estos filtros.")
                        TextButton(onClick = vm::clearFilters) { Text("Ver todos los contenidos") }
                    }
                }
            }
            items(s.items, key = { it.id }) { item ->
                LearnRow(item, Modifier.animateItem()) { onOpenContent(item.id) }
            }
            if (s.suggestions.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(5.dp))
                    Text("También puedes explorar", style = MaterialTheme.typography.titleMedium)
                    Text("Otras opciones de ${s.kind.label.lowercase()} fuera del filtro actual.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(s.suggestions, key = { "suggested-${it.id}" }) { item ->
                    LearnRow(item, Modifier.animateItem()) { onOpenContent(item.id) }
                }
            }
        }
    }
}

@Composable
fun SearchField(value: String, onChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (value.isNotEmpty()) IconButton(onClick = { onChange("") }) { Icon(Icons.Filled.Close, "Limpiar búsqueda") }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = Color.Transparent,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

@Composable
fun CategoryChips(categories: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { CategoryChip("Todo", selected == null) { onSelect(null) } }
        items(categories) { c -> CategoryChip(c, selected == c) { onSelect(c) } }
    }
}

@Composable
private fun CategoryChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, Modifier.padding(vertical = 8.dp)) },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            selectedContainerColor = LocalExtraColors.current.hero,
            selectedLabelColor = Color.White,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color.Transparent,
            selectedBorderColor = Color.Transparent,
        ),
    )
}

@Composable
private fun LearnRow(item: LearnItem, modifier: Modifier, onClick: () -> Unit) {
    MMCard(modifier, onClick = onClick) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            FoodPhoto(item.id, Modifier.size(width = 112.dp, height = 100.dp).clip(MaterialTheme.shapes.small))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(item.kind.label.dropLast(1).uppercase(), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text(item.title, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${item.category} · ${item.duration}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Detalle de ficha técnica, receta o curso y recurso nativo de compartir. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnDetailScreen(
    onBack: () -> Unit,
    onOpenCatalog: () -> Unit,
    vm: LearnDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val learning by vm.learning.collectAsStateWithLifecycle()
    val item = vm.state.item
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.kind?.label ?: "Contenido") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
                actions = {
                    if (item != null) IconButton(onClick = {
                        val ok = ContactHelper.share(
                            context, item.title,
                            "Mira \"${item.title}\" en la app Master Martini (${item.category} · ${item.duration}).",
                        )
                        if (!ok) scope.launch { snackbar.showSnackbar("No hay apps para compartir") }
                    }) { Icon(Icons.Outlined.Share, "Compartir") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (item == null) {
            EmptyState("Contenido no encontrado", Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FoodPhoto(item.id, Modifier.fillMaxWidth().height(220.dp).clip(MaterialTheme.shapes.large))
            TextButton(onClick = vm::favorite) { Text(if (item.id in learning.favorites) "Guardado · Quitar favorito" else "Guardar favorito") }
            if (item.kind == LearnKind.COURSE) {
                val progress = learning.progress(item.id, item.steps.size)
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                Text("${(progress * 100).toInt()} % completado · Avance guardado en este dispositivo")
            }
            Text(item.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                "${item.author} · ${item.category} · ${item.duration} · ${item.difficulty.label}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(item.description, style = MaterialTheme.typography.bodyLarge)
            if (item.ingredients.isNotEmpty()) {
                Text("Ingredientes", style = MaterialTheme.typography.titleMedium)
                item.ingredients.forEach { Text("• $it", style = MaterialTheme.typography.bodyLarge) }
            }
            item.sourceUrl?.let { url -> TextButton(onClick = {
                if (!ContactHelper.openSource(context, url)) scope.launch { snackbar.showSnackbar("No hay navegador disponible") }
            }) { Text("Consultar receta de referencia") } }
            Text(if (item.kind == LearnKind.COURSE) "Módulos · Marca lo que completaste" else "Guía de lectura", style = MaterialTheme.typography.titleMedium)
            item.steps.forEachIndexed { i, step ->
                Row(verticalAlignment = Alignment.Top) {
                    if (item.kind == LearnKind.COURSE) Checkbox(checked = learning.completed(item.id, i), onCheckedChange = { vm.module(i) })
                    Box(
                        Modifier.size(26.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Text("${i + 1}", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelMedium) }
                    Spacer(Modifier.width(10.dp))
                    Text(step, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 2.dp))
                }
            }
            if (vm.state.products.isNotEmpty()) {
                MMCard(container = MaterialTheme.colorScheme.primaryContainer) {
                    Column(Modifier.padding(16.dp)) {
                        Pill("Insumos de este contenido", Color.Transparent, MaterialTheme.colorScheme.primary)
                        Text(
                            vm.state.products.joinToString(" · ") { it.name },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 6.dp),
                        )
                        TextButton(onClick = onOpenCatalog, contentPadding = PaddingValues(0.dp)) {
                            Text("Ver en catálogo", textDecoration = TextDecoration.Underline)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
