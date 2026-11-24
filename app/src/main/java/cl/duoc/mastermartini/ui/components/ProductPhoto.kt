package cl.duoc.mastermartini.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import cl.duoc.mastermartini.data.model.Product
import coil.compose.SubcomposeAsyncImage

/**
 * Foto del producto descargada del backend (Coil la guarda en caché).
 * Mientras carga, o si no hay conexión, se muestra una fotografía referencial incluida en el APK.
 */
@Composable
fun ProductPhoto(product: Product, modifier: Modifier = Modifier) {
    val url = product.imagePath
    if (url.isNullOrBlank()) {
        FoodPhoto(product.category, modifier)
        return
    }
    SubcomposeAsyncImage(
        model = url,
        contentDescription = product.name,
        contentScale = ContentScale.Crop,
        modifier = modifier,
        loading = { FoodPhoto(product.category, Modifier.fillMaxSize()) },
        error = { FoodPhoto(product.category, Modifier.fillMaxSize()) },
    )
}
