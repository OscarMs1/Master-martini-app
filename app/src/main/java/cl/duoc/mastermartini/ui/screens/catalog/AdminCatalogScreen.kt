package cl.duoc.mastermartini.ui.screens.catalog

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.mastermartini.data.model.Product
import cl.duoc.mastermartini.data.model.formatClp
import cl.duoc.mastermartini.data.remote.ApiException
import cl.duoc.mastermartini.data.repository.ProductCategory
import cl.duoc.mastermartini.data.repository.ProductDraft
import cl.duoc.mastermartini.data.repository.ProductRepository
import cl.duoc.mastermartini.device.CatalogPhotoStore
import cl.duoc.mastermartini.ui.components.ProductPhoto
import kotlinx.coroutines.launch

/** Panel de administración: crea, edita y elimina productos del catálogo real (backend). */
@Composable
fun AdminCatalogScreen(repository: ProductRepository, isAdmin: Boolean, onBack: () -> Unit) {
    val products by repository.products.collectAsStateWithLifecycle()
    val status by repository.status.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var editing by remember { mutableStateOf<Product?>(null) }
    var createNew by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Product?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    // Esta comprobación protege la navegación directa; el backend vuelve a comprobar el rol en cada cambio.
    if (!isAdmin) {
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(22.dp)) {
            Text("Acceso restringido", style = MaterialTheme.typography.headlineSmall)
            Text("Ingresa con una cuenta de administración para modificar productos.")
            TextButton(onClick = onBack) { Text("Volver") }
        }
        return
    }

    LaunchedEffect(Unit) { repository.refresh() }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Volver a Mi cuenta")
            }
            Text("Panel de administración", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            if (status.loading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            else IconButton(onClick = { scope.launch { repository.refresh() } }) {
                Icon(Icons.Outlined.Refresh, contentDescription = "Actualizar catálogo")
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Catálogo de productos", style = MaterialTheme.typography.headlineSmall)
                        Text("${products.size} productos publicados · los cambios se ven también en la página web",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        (message ?: status.error)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        Button(onClick = { createNew = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Add, null)
                            Text("  Agregar producto")
                        }
                    }
                }
            }
            items(products, key = { it.id }) { product ->
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProductPhoto(product, Modifier.size(78.dp).clip(RoundedCornerShape(12.dp)))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(product.name, style = MaterialTheme.typography.titleSmall)
                            Text("${product.category} · ${product.formats.joinToString(" · ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                formatClp(product.finalPrice) +
                                    (if (product.discount > 0) " (-${product.discount} %)" else "") +
                                    " · stock ${product.stock}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { editing = product }) {
                                    Icon(Icons.Outlined.Edit, null, Modifier.size(16.dp))
                                    Text(" Editar")
                                }
                                TextButton(onClick = { toDelete = product }) {
                                    Icon(Icons.Outlined.DeleteOutline, null, Modifier.size(16.dp))
                                    Text(" Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (createNew || editing != null) {
        EditProductDialog(
            product = editing,
            onDismiss = { createNew = false; editing = null },
            onSave = { draft, photo ->
                repository.save(editing, draft, photo)
                message = null
                createNew = false
                editing = null
            },
        )
    }
    toDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar producto") },
            text = { Text("¿Eliminar «${product.name}» del catálogo? También se quitará de la página web.") },
            confirmButton = {
                TextButton(onClick = {
                    toDelete = null
                    scope.launch {
                        message = try { repository.delete(product); null } catch (e: ApiException) { e.message }
                    }
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun EditProductDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: suspend (ProductDraft, ByteArray?) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val key = product?.id
    var name by remember(key) { mutableStateOf(product?.name.orEmpty()) }
    var category by remember(key) {
        mutableStateOf(product?.let { ProductCategory.fromLabel(it.category) } ?: ProductCategory.PANADERIA)
    }
    var format by remember(key) { mutableStateOf(product?.formats?.firstOrNull() ?: "1 kg") }
    var description by remember(key) { mutableStateOf(product?.description.orEmpty()) }
    var price by remember(key) { mutableStateOf(product?.price?.toString().orEmpty()) }
    var discount by remember(key) { mutableStateOf(product?.discount?.toString() ?: "0") }
    var stock by remember(key) { mutableStateOf(product?.stock?.toString() ?: "0") }
    var featured by remember(key) { mutableStateOf(product?.featured ?: false) }
    var imageUri by remember(key) { mutableStateOf<Uri?>(null) }
    var saving by remember(key) { mutableStateOf(false) }
    var error by remember(key) { mutableStateOf<String?>(null) }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { imageUri = it }
    val preview = remember(imageUri) {
        imageUri?.let { uri ->
            runCatching { context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } }.getOrNull()
        }
    }

    // Mismas reglas que el backend (ProductoRequest).
    val priceValue = price.toLongOrNull()
    val discountValue = discount.toIntOrNull()
    val stockValue = stock.toIntOrNull()
    val valid = name.isNotBlank() && format.isNotBlank() && description.isNotBlank() &&
        priceValue != null && priceValue in 1..100_000_000 &&
        discountValue != null && discountValue in 0..90 &&
        stockValue != null && stockValue in 0..1_000_000
    val digits = KeyboardOptions(keyboardType = KeyboardType.Number)

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(if (product == null) "Nuevo producto" else "Editar producto") },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    when {
                        preview != null -> Image(preview.asImageBitmap(), "Nueva fotografía",
                            Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                        product != null -> ProductPhoto(product, Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)))
                        else -> Icon(Icons.Outlined.Image, null, Modifier.size(64.dp))
                    }
                    OutlinedButton(onClick = {
                        pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) { Text("Elegir imagen") }
                }
                OutlinedTextField(value = name, onValueChange = { name = it.take(120) }, label = { Text("Nombre") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Categoría", style = MaterialTheme.typography.labelMedium)
                ProductCategory.entries.forEach { option ->
                    FilterChip(selected = category == option, onClick = { category = option }, label = { Text(option.label) })
                }
                OutlinedTextField(value = format, onValueChange = { format = it.take(80) },
                    label = { Text("Formato (ej: Saco 25 kg)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it.filter(Char::isDigit).take(9) },
                    label = { Text("Precio en pesos") }, keyboardOptions = digits, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = discount, onValueChange = { discount = it.filter(Char::isDigit).take(2) },
                        label = { Text("Descuento %") }, keyboardOptions = digits, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = stock, onValueChange = { stock = it.filter(Char::isDigit).take(7) },
                        label = { Text("Stock") }, keyboardOptions = digits, singleLine = true, modifier = Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = featured, onCheckedChange = { featured = it })
                    Text("Destacado en la portada")
                }
                OutlinedTextField(value = description, onValueChange = { description = it.take(1500) },
                    label = { Text("Descripción") }, minLines = 2, maxLines = 4, modifier = Modifier.fillMaxWidth())
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(enabled = valid && !saving, onClick = {
                saving = true
                error = null
                scope.launch {
                    val photo = imageUri?.let { CatalogPhotoStore.toJpeg(context, it) }
                    if (imageUri != null && photo == null) {
                        error = "No se pudo leer la imagen elegida"
                    } else try {
                        onSave(
                            ProductDraft(name, category, format, description, priceValue!!, discountValue!!, stockValue!!, featured),
                            photo,
                        )
                    } catch (e: ApiException) {
                        error = e.message
                    }
                    saving = false
                }
            }) { Text(if (saving) "Guardando…" else "Guardar") }
        },
        dismissButton = { TextButton(enabled = !saving, onClick = onDismiss) { Text("Cancelar") } },
    )
}
