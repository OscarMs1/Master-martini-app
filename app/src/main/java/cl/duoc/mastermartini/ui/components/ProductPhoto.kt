package cl.duoc.mastermartini.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import cl.duoc.mastermartini.data.model.Product
import cl.duoc.mastermartini.device.CatalogPhotoStore

/** Fotografía personalizada del administrador o fotografía referencial incluida en el APK. */
@Composable
fun ProductPhoto(product: Product, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember(product.imagePath) { CatalogPhotoStore.read(context, product.imagePath) }
    if (bitmap != null) {
        Image(bitmap = bitmap.asImageBitmap(), contentDescription = product.name,
            contentScale = ContentScale.Crop, modifier = modifier)
    } else {
        FoodPhoto(if (product.id.startsWith("local-")) product.category else product.id, modifier)
    }
}
