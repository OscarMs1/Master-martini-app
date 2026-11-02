package cl.duoc.mastermartini.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mastermartini.ui.AppViewModelProvider
import cl.duoc.mastermartini.ui.components.BrandHeader
import cl.duoc.mastermartini.ui.components.MMCard
import cl.duoc.mastermartini.ui.components.SectionHeader

private val specialties = listOf("Panadería", "Pastelería", "Chocolatería", "Heladería")

@Composable
fun HomeScreen(
    onOpenContent: (String) -> Unit,
    onSeeAllRecipes: () -> Unit,
    onExploreCategory: (String) -> Unit,
    onOpenServices: () -> Unit,
    vm: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            BrandHeader {
                OutlinedButton(onClick = vm::toggleLargeText) { Text("Aa") }
            }
        }
        item {
            Text(
                "Hola, ${state.user?.firstName?.takeIf { it.isNotBlank() } ?: "bienvenido"}",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                "Descubre técnicas y recetas para tu negocio",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            FeaturedVideoCard()
        }
        item {
            SectionHeader("Recetas de temporada", "Ver todas", onSeeAllRecipes)
            Spacer(Modifier.height(8.dp))
            // Dos columnas de igual peso: las dos recetas quedan completas y visibles.
            val recipes = featuredSeasonalRecipes(state.recipes)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                recipes.forEach { recipe ->
                    RecipePreviewCard(
                        recipe = recipe,
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenContent(recipe.id) },
                    )
                }
            }
        }
        item {
            SectionHeader("Explora por especialidad")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                specialties.take(2).forEach { category ->
                    SpecialtyCard(category, Modifier.weight(1f)) { onExploreCategory(category) }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                specialties.drop(2).forEach { category ->
                    SpecialtyCard(category, Modifier.weight(1f)) { onExploreCategory(category) }
                }
            }
        }
        item {
            MMCard(onClick = { onOpenContent("c-vitrina") }, container = MaterialTheme.colorScheme.primaryContainer) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("APRENDE A TU RITMO", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                    Text("Pastelería de vitrina", style = MaterialTheme.typography.titleMedium)
                    Text("Tres módulos y seguimiento de tu avance, guardado en el dispositivo.", style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { onOpenContent("c-vitrina") }, contentPadding = PaddingValues(0.dp)) {
                        Text("Abrir curso")
                    }
                }
            }
        }
        item {
            SectionHeader("Eventos y asesorías", "Ver servicios", onOpenServices)
            Text(
                "Consulta actividades de demostración y guarda solicitudes locales.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
