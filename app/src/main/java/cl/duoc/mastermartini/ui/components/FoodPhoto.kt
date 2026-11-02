package cl.duoc.mastermartini.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import cl.duoc.mastermartini.R

/**
 * Fotografías reales de referencia para el MVP académico.
 * Cada uno de los nueve productos del catálogo tiene una foto original distinta.
 * Son fotografías ilustrativas, no imágenes oficiales de envases Master Martini.
 */
private val illustrationById = mapOf(
    // Catálogo: nueve recursos fotográficos independientes aportados por el equipo.
    "p-crema" to R.drawable.catalog_crema_vegetal,
    "p-cobertura" to R.drawable.catalog_cobertura_semiamarga,
    "p-margarina" to R.drawable.catalog_margarina_hojaldre,
    "p-brillo" to R.drawable.catalog_brillo_neutro,
    "p-manteca-cacao" to R.drawable.catalog_manteca_cacao,
    "p-mejorador" to R.drawable.catalog_mejorador_masas,
    "p-base-helado" to R.drawable.catalog_base_helado,
    "p-pasta-pistacho" to R.drawable.catalog_pasta_pistacho,
    "p-salsa-bechamel" to R.drawable.catalog_salsa_bechamel,

    // Biblioteca y portada: recursos originales ya presentes en el proyecto.
    "r-croissant" to R.drawable.photo_croissant,
    "r-kuchen" to R.drawable.photo_cake,
    "r-galletas" to R.drawable.photo_cookies,
    "r-bombones" to R.drawable.photo_chocolate,
    "r-ganache" to R.drawable.catalog_cobertura_semiamarga,
    "r-copa-helado" to R.drawable.photo_icecream,
    "v-templado" to R.drawable.photo_chocolate,
    "v-laminado" to R.drawable.photo_croissant,
    "v-helado" to R.drawable.photo_icecream,
    "v-vitrina" to R.drawable.photo_chef,
    "v-horneo" to R.drawable.photo_cookies,
    "c-hoja" to R.drawable.photo_croissant,
    "c-vitrina" to R.drawable.photo_chef,
    "c-chocolate" to R.drawable.photo_chocolate,
    "Panadería" to R.drawable.catalog_mejorador_masas,
    "Pastelería" to R.drawable.catalog_brillo_neutro,
    "Chocolatería" to R.drawable.catalog_base_helado,
    "Heladería" to R.drawable.catalog_pasta_pistacho,
    "Gelatería" to R.drawable.photo_icecream,
    "Galletas" to R.drawable.photo_cookies,
    "Food service" to R.drawable.photo_chef,
    "chef" to R.drawable.photo_chef,
    "home-video" to R.drawable.catalog_cobertura_semiamarga,
)

@Composable
fun FoodPhoto(id: String, modifier: Modifier = Modifier) {
    val imageRes = illustrationById[id]
    if (imageRes == null) {
        // No presentar una foto gastronómica de otro producto por error.
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.ImageNotSupported,
                contentDescription = "Imagen no disponible",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    }
}
