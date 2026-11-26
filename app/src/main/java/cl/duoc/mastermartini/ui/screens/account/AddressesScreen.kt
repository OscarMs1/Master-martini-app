package cl.duoc.mastermartini.ui.screens.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mastermartini.data.repository.AddressRepository
import cl.duoc.mastermartini.ui.AppViewModelProvider
import cl.duoc.mastermartini.ui.components.ChoiceField
import cl.duoc.mastermartini.ui.components.EmptyState
import cl.duoc.mastermartini.ui.components.MMCard
import cl.duoc.mastermartini.ui.components.MMTextField
import cl.duoc.mastermartini.ui.components.PrimaryButton
import cl.duoc.mastermartini.ui.components.shake

/** Direcciones guardadas de la cuenta: se usan para rellenar el despacho al comprar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressesScreen(
    onBack: () -> Unit,
    vm: AddressesViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis direcciones") },
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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "Guarda hasta ${AddressRepository.MAX} direcciones de despacho. Las verás al confirmar un pedido, " +
                    "aquí y en la página web.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (s.addresses.isEmpty() && !s.showForm) EmptyState("Aún no tienes direcciones guardadas.")
            s.addresses.forEach { a ->
                MMCard {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Place, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(a.label, style = MaterialTheme.typography.titleSmall)
                            Text(a.address, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${a.comuna}, ${a.region}",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                listOf(a.contactName, a.phone).filter { it.isNotBlank() }.joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { vm.askDelete(a) }) { Icon(Icons.Outlined.DeleteOutline, "Eliminar ${a.label}") }
                    }
                }
            }

            if (!s.showForm) {
                if (s.canAdd) OutlinedButton(onClick = vm::openForm, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Add, null); Spacer(Modifier.width(6.dp)); Text("Agregar dirección")
                } else Text(
                    "Llegaste al máximo de ${AddressRepository.MAX}. Elimina una para agregar otra.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(s.showForm) {
                // Sin tarjeta: los campos son blancos y se perderían sobre una tarjeta blanca.
                run {
                    Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Nueva dirección", style = MaterialTheme.typography.titleMedium)
                        MMTextField("Nombre de la dirección", s.label, vm::onLabel, Icons.Outlined.Bookmark, placeholder = "Ej: Local Providencia")
                        MMTextField("Quién recibe", s.contactName, vm::onContact, Icons.Outlined.Person)
                        MMTextField("Teléfono", s.phone, vm::onPhone, Icons.Outlined.Phone, keyboardType = KeyboardType.Phone)
                        ChoiceField("Región", s.region, vm.regions.names, vm::onRegion, Icons.Outlined.Map)
                        ChoiceField("Comuna", s.comuna, vm.regions.comunas(s.region.value), vm::onComuna, Icons.Outlined.LocationCity)
                        MMTextField("Dirección", s.address, vm::onAddress, Icons.Outlined.Home, placeholder = "Ej: Av. Matta 1234, local 2")
                        s.formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        PrimaryButton(
                            "Guardar dirección", vm::save, loading = s.saving, icon = Icons.Outlined.Save,
                            modifier = Modifier.shake(s.failedAttempts),
                        )
                        TextButton(onClick = vm::closeForm, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Cancelar") }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    s.deleting?.let { a ->
        AlertDialog(
            onDismissRequest = vm::dismissDelete,
            title = { Text("¿Eliminar ${a.label}?") },
            text = { Text("${a.address}, ${a.comuna}. Esta dirección también dejará de aparecer en la página web.") },
            confirmButton = { TextButton(onClick = vm::confirmDelete) { Text("Eliminar", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = vm::dismissDelete) { Text("Cancelar") } },
        )
    }
}
