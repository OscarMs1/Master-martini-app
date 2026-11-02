package cl.duoc.mastermartini.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.mastermartini.data.model.LearnItem
import cl.duoc.mastermartini.ui.components.FoodPhoto
import cl.duoc.mastermartini.ui.components.MMCard

/** Dos recetas iguales en altura, con imágenes y titulares alineados. */
@Composable
internal fun RecipePreviewCard(
    recipe: LearnItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    MMCard(modifier = modifier, onClick = onClick) {
        FoodPhoto(recipe.id, Modifier.fillMaxWidth().height(104.dp))
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(recipe.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(
                when (recipe.id) {
                    "r-kuchen" -> "Torta decorada"
                    "r-bombones" -> "Frutillas con chocolate"
                    else -> recipe.title
                },
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(recipe.duration, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Las imágenes de especialidad no usan las mismas fotos de las recetas de portada. */
@Composable
internal fun SpecialtyCard(category: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    MMCard(modifier = modifier, onClick = onClick) {
        FoodPhoto(category, Modifier.fillMaxWidth().height(100.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)))
        Text(category, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.titleSmall)
    }
}
