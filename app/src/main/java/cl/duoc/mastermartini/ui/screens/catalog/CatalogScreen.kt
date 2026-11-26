package cl.duoc.mastermartini.ui.screens.catalog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mastermartini.data.model.Product
import cl.duoc.mastermartini.data.model.QuoteLine
import cl.duoc.mastermartini.ui.AppViewModelProvider
import cl.duoc.mastermartini.ui.components.CapsuleButton
import cl.duoc.mastermartini.ui.components.ChipGroup
import cl.duoc.mastermartini.ui.components.EmptyState
import cl.duoc.mastermartini.ui.components.ProductPhoto
import cl.duoc.mastermartini.ui.components.FieldMessage
import cl.duoc.mastermartini.ui.components.MMCard
import cl.duoc.mastermartini.ui.components.PrimaryButton
import cl.duoc.mastermartini.ui.components.ScreenTitle
import cl.duoc.mastermartini.ui.components.shake
import cl.duoc.mastermartini.ui.screens.learn.CategoryChips
import cl.duoc.mastermartini.ui.screens.learn.SearchField
import cl.duoc.mastermartini.ui.theme.LocalExtraColors
import cl.duoc.mastermartini.data.model.formatClp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun CatalogScreen(
    onSendQuote: () -> Unit,
    onOpenReviews: (productId: String) -> Unit = {},
    vm: CatalogViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        vm.messages.collect { android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show() }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ScreenTitle("Catálogo", Modifier.padding(horizontal = 20.dp, vertical = 12.dp), subtitle = "Catálogo Master Martini · precios con IVA incluido")
            SearchField(s.query, vm::onQuery, "Buscar producto", Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(12.dp))
            CategoryChips(s.categories, s.category, vm::onCategory)
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                s.error?.let { msg ->
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            EmptyState(msg)
                            CapsuleButton("Reintentar", onClick = vm::refresh)
                        }
                    }
                }
                if (s.loading && s.products.isEmpty()) item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else if (s.products.isEmpty() && s.error == null) item { EmptyState("No hay productos que coincidan.") }
                items(s.products, key = { it.id }) { p ->
                    ProductRow(
                        p, s.lines[p.id], favorite = p.id in s.favorites, modifier = Modifier.animateItem(),
                        onFavorite = { vm.toggleFavorite(p) },
                        onReviews = { onOpenReviews(p.id) },
                    ) { vm.openSheet(p) }
                }
            }
        }

        // Barra de cotización: entra desde abajo cuando hay al menos un producto.
        AnimatedVisibility(
            visible = s.quoteCount > 0,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Surface(
                onClick = onSendQuote,
                color = LocalExtraColors.current.hero,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
            ) {
                Row(Modifier.padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Cotización · ", style = MaterialTheme.typography.titleSmall)
                    AnimatedContent(
                        targetState = s.quoteCount,
                        transitionSpec = { (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut()) },
                        label = "quoteCount",
                    ) { n -> Text("$n producto${if (n == 1) "" else "s"}", style = MaterialTheme.typography.titleSmall) }
                    Spacer(Modifier.weight(1f))
                    Text("Revisar", color = LocalExtraColors.current.badge, style = MaterialTheme.typography.titleSmall)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = LocalExtraColors.current.badge)
                }
            }
        }
    }

    s.sheet?.let { form ->
        AddToQuoteSheet(
            form = form,
            onDismiss = vm::closeSheet,
            onFormat = vm::onFormat,
            onQuantity = vm::onQuantity,
            onInc = vm::incQuantity,
            onConfirm = vm::confirmSheet,
            onRemove = { vm.removeFromQuote(form.product.id) },
        )
    }
}

@Composable
private fun ProductRow(
    product: Product,
    line: QuoteLine?,
    favorite: Boolean,
    modifier: Modifier,
    onFavorite: () -> Unit,
    onReviews: () -> Unit,
    onAdd: () -> Unit,
) {
    val extra = LocalExtraColors.current
    MMCard(modifier) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductPhoto(product, Modifier.size(96.dp).clip(MaterialTheme.shapes.medium))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(product.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f).padding(top = 4.dp))
                    IconButton(onClick = onFavorite, modifier = Modifier.size(32.dp)) {
                        if (favorite) Icon(Icons.Filled.Favorite, "Quitar de favoritos", tint = MaterialTheme.colorScheme.error)
                        else Icon(Icons.Outlined.FavoriteBorder, "Guardar en favoritos")
                    }
                }
                Text(
                    "Formato${if (product.formats.size > 1) "s" else ""}: ${product.formats.joinToString(" · ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(formatClp(product.finalPrice), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    if (product.discount > 0) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            formatClp(product.price),
                            style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("-${product.discount} %", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
                Spacer(Modifier.height(2.dp))
                TextButton(
                    onClick = onReviews,
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp),
                ) {
                    Icon(Icons.Outlined.StarBorder, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ver opiniones", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(4.dp))
                // Acción que abre el selector de cantidad y formato.
                AnimatedContent(
                    targetState = line,
                    transitionSpec = { (scaleIn() + fadeIn()) togetherWith fadeOut() using SizeTransform(clip = false) },
                    label = "addButton",
                ) { current ->
                    if (current == null && product.stock <= 0) {
                        Text("Sin stock por ahora", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                    } else if (current == null) {
                        CapsuleButton("Agregar", onClick = onAdd)
                    } else {
                        Surface(
                            onClick = onAdd,
                            color = extra.successContainer,
                            contentColor = extra.success,
                            shape = CircleShape,
                        ) {
                            Row(Modifier.padding(horizontal = 14.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Check, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Agregado (${current.quantity} × ${current.format})", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToQuoteSheet(
    form: AddToQuoteForm,
    onDismiss: () -> Unit,
    onFormat: (String) -> Unit,
    onQuantity: (String) -> Unit,
    onInc: (Int) -> Unit,
    onConfirm: () -> Unit,
    onRemove: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(form.product.name, style = MaterialTheme.typography.titleLarge)
            Text(form.product.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ChipGroup(
                label = "Formato",
                options = form.product.formats,
                isSelected = { it == form.format },
                onToggle = onFormat,
                optionLabel = { it },
                error = form.formatError,
            )
            Column {
                Text("Cantidad", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = { onInc(-1) }) { Icon(Icons.Filled.Remove, "Restar uno") }
                    OutlinedTextField(
                        value = form.quantity.value,
                        onValueChange = onQuantity,
                        isError = form.quantity.showError,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(96.dp).padding(horizontal = 8.dp),
                        shape = MaterialTheme.shapes.medium,
                    )
                    FilledTonalIconButton(onClick = { onInc(1) }) { Icon(Icons.Filled.Add, "Sumar uno") }
                }
                FieldMessage(error = if (form.quantity.showError) form.quantity.error else null)
            }
            PrimaryButton(
                text = if (form.isEditing) "Actualizar cotización" else "Agregar a cotización",
                onClick = onConfirm,
                icon = Icons.Filled.Check,
                modifier = Modifier.shake(form.failedAttempts),
            )
            if (form.isEditing) {
                TextButton(onClick = onRemove, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Icon(Icons.Outlined.DeleteOutline, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Quitar de la cotización", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
