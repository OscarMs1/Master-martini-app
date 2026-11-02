package cl.duoc.mastermartini.ui.screens.catalog

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.mastermartini.data.model.Product
import cl.duoc.mastermartini.data.repository.LocalProductRepository
import cl.duoc.mastermartini.device.CatalogPhotoStore
import cl.duoc.mastermartini.ui.components.ProductPhoto
import java.util.UUID

private val categories = listOf("Pastelería", "Panadería", "Gelatería", "Food service")

@Composable
fun AdminCatalogScreen(repository: LocalProductRepository, isAdmin: Boolean, onBack: () -> Unit) {
    val products by repository.products.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Product?>(null) }
    var createNew by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Product?>(null) }
    var confirmRestore by remember { mutableStateOf(false) }

    // Esta comprobación también protege la navegación directa al editor.
    if (!isAdmin) {
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(22.dp)) {
            Text("Acceso restringido", style = MaterialTheme.typography.headlineSmall)
            Text("Ingresa con la cuenta de administración para modificar productos.")
            TextButton(onClick = onBack) { Text("Volver") }
        }
        return
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 20.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Volver a Mi cuenta")
            }
            Text("Panel de administración", style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f))
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Catálogo de productos", style = MaterialTheme.typography.headlineSmall)
                        Text("${products.size} productos en el catálogo",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { createNew = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Add, null)
                            Text("  Agregar producto")
                        }
                        TextButton(onClick = { confirmRestore = true }) { Text("Restaurar catálogo inicial") }
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
            onSave = { repository.save(it); createNew = false; editing = null },
        )
    }
    toDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar producto") },
            text = { Text("¿Eliminar «${product.name}» del catálogo?") },
            confirmButton = { TextButton(onClick = { repository.delete(product.id); toDelete = null }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } },
        )
    }
    if (confirmRestore) {
        AlertDialog(
            onDismissRequest = { confirmRestore = false },
            title = { Text("Restaurar productos") },
            text = { Text("Se reemplazarán tus cambios locales por los productos iniciales.") },
            confirmButton = { TextButton(onClick = { repository.restoreExamples(); confirmRestore = false }) { Text("Restaurar") } },
            dismissButton = { TextButton(onClick = { confirmRestore = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun EditProductDialog(product: Product?, onDismiss: () -> Unit, onSave: (Product) -> Unit) {
    val context = LocalContext.current
    var name by remember(product?.id) { mutableStateOf(product?.name.orEmpty()) }
    var category by remember(product?.id) { mutableStateOf(product?.category ?: categories.first()) }
    var formats by remember(product?.id) { mutableStateOf(product?.formats?.joinToString(", ") ?: "1 kg") }
    var description by remember(product?.id) { mutableStateOf(product?.description.orEmpty()) }
    var imageUri by remember(product?.id) { mutableStateOf<Uri?>(null) }
    var photoError by remember(product?.id) { mutableStateOf(false) }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        imageUri = it
    }
    val preview = remember(imageUri) {
        imageUri?.let { uri ->
            runCatching { context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } }.getOrNull()
        }
    }
    val parsedFormats = formats.split(',').map(String::trim).filter(String::isNotEmpty)
    val valid = name.isNotBlank() && parsedFormats.isNotEmpty() && description.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Nuevo producto" else "Editar producto") },
        text = {
            Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (preview != null) {
                        Image(preview.asImageBitmap(), "Nueva fotografía", Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                    } else if (product != null) {
                        ProductPhoto(product, Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)))
                    } else {
                        Icon(Icons.Outlined.Image, null, Modifier.size(64.dp))
                    }
                    OutlinedButton(onClick = {
                        pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) { Text("Elegir imagen") }
                }
                if (photoError) Text("No se pudo guardar la imagen", color = MaterialTheme.colorScheme.error)
                OutlinedTextField(value = name, onValueChange = { name = it.take(70) }, label = { Text("Nombre") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Categoría", style = MaterialTheme.typography.labelMedium)
                categories.forEach { option ->
                    FilterChip(selected = category == option, onClick = { category = option }, label = { Text(option) })
                }
                OutlinedTextField(value = formats, onValueChange = { formats = it.take(70) },
                    label = { Text("Formatos (separados por coma)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it.take(160) },
                    label = { Text("Descripción") }, minLines = 2, maxLines = 3, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(enabled = valid, onClick = {
                val photoPath = if (imageUri == null) product?.imagePath else CatalogPhotoStore.save(context, imageUri!!)
                if (imageUri != null && photoPath == null) photoError = true
                else onSave(Product(
                    id = product?.id ?: "local-${UUID.randomUUID()}",
                    name = name.trim(), category = category,
                    formats = parsedFormats, description = description.trim(), imagePath = photoPath,
                ))
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
