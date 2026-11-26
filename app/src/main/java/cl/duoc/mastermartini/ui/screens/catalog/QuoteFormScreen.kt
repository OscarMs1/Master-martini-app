package cl.duoc.mastermartini.ui.screens.catalog

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mastermartini.MasterMartiniApp
import cl.duoc.mastermartini.device.ContactHelper
import cl.duoc.mastermartini.device.rememberNotificationPermission
import cl.duoc.mastermartini.domain.validation.FieldState
import cl.duoc.mastermartini.domain.validation.Validators
import cl.duoc.mastermartini.ui.AppViewModelProvider
import cl.duoc.mastermartini.ui.components.DateField
import cl.duoc.mastermartini.ui.components.EmptyState
import cl.duoc.mastermartini.ui.components.MMCard
import cl.duoc.mastermartini.ui.components.MMDivider
import cl.duoc.mastermartini.ui.components.MMTextField
import cl.duoc.mastermartini.ui.components.PrimaryButton
import cl.duoc.mastermartini.ui.components.shake
import cl.duoc.mastermartini.ui.theme.LocalExtraColors
import cl.duoc.mastermartini.data.model.formatClp
import cl.duoc.mastermartini.data.repository.Delivery
import cl.duoc.mastermartini.data.repository.OrderEstimate
import cl.duoc.mastermartini.ui.components.ChoiceField
import cl.duoc.mastermartini.ui.components.SegmentedChoice
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Checkbox
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteFormScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    vm: QuoteFormViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val lines by vm.lines.collectAsStateWithLifecycle()
    val suggestions by vm.suggestions.collectAsStateWithLifecycle()
    val saved by vm.savedAddresses.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }
    val askNotifications = rememberNotificationPermission()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmar pedido") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Resumen de productos (la tarjeta se redimensiona con animación al quitar ítems).
            MMCard(Modifier.animateContentSize()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Productos (${lines.size})", style = MaterialTheme.typography.titleSmall)
                    if (lines.isEmpty()) EmptyState("Tu cotización está vacía. Agrega productos desde el catálogo.")
                    lines.forEachIndexed { i, priced ->
                        val line = priced.line
                        if (i > 0) MMDivider()
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(line.productName, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${line.quantity} × ${line.format}" + (priced.unitPrice?.let { " · ${formatClp(it)} c/u" } ?: ""),
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            priced.unitPrice?.let { Text(formatClp(priced.subtotal), style = MaterialTheme.typography.titleSmall) }
                            IconButton(onClick = { vm.removeLine(line.productId) }) {
                                Icon(Icons.Outlined.Close, "Quitar ${line.productName}")
                            }
                        }
                    }
                }
            }

            // Sugerencias según lo que hay en el carrito.
            ProductSuggestionsRow(suggestions.products, cartEmpty = lines.isEmpty(), onAdd = vm::addSuggestion)
            ClassSuggestionsCard(suggestions.classes, onEnroll = vm::askClass)

            Text("Datos de despacho", style = MaterialTheme.typography.titleMedium)
            SegmentedChoice("Entrega", Delivery.entries, s.delivery, vm::onDelivery, { it.label })
            if (s.delivery == Delivery.DOMICILIO) SavedAddressChips(saved, s.selectedAddressId, vm::applyAddress)
            MMTextField("Nombre de contacto", s.contactName, vm::onContact, Icons.Outlined.Person, onFocusLost = vm::onContactBlur)
            MMTextField(
                "Teléfono", s.phone, vm::onPhone, Icons.Outlined.Phone,
                keyboardType = KeyboardType.Phone, onFocusLost = vm::onPhoneBlur,
            )
            ChoiceField("Región", s.region, vm.regions.names, vm::onRegion, Icons.Outlined.Map)
            ChoiceField("Comuna", s.comuna, vm.regions.comunas(s.region.value), vm::onComuna, Icons.Outlined.LocationCity)
            MMTextField(
                "Dirección", s.address, vm::onAddress, Icons.Outlined.Home,
                placeholder = "Ej: Av. Matta 1234, local 2", onFocusLost = vm::onAddressBlur,
            )
            if (vm.canSaveAddress(s, saved)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = s.saveAddress, onCheckedChange = vm::onSaveAddress)
                    Text("Guardar en Mis direcciones para la próxima compra", style = MaterialTheme.typography.bodyMedium)
                }
            }
            DateField(
                label = if (s.delivery == Delivery.RETIRO) "Fecha de retiro deseada" else "Fecha de despacho deseada",
                valueLabel = s.dateLabel,
                selectedMillis = s.dateMillis,
                error = s.dateError,
                onDateSelected = vm::onDate,
                isSelectable = { Validators.futureWorkingDate(it).isValid },
            )
            MMTextField(
                "Comentarios (opcional)", s.notes, vm::onNotes, Icons.AutoMirrored.Outlined.Notes,
                singleLine = false, minLines = 3, imeAction = ImeAction.Default,
                helper = "${s.notes.value.length}/300 · se guardan en este teléfono",
            )

            val estimate = vm.estimate(lines, s.delivery)
            MMCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TotalRow("Subtotal", formatClp(estimate.subtotal))
                    TotalRow("Despacho", if (estimate.shipping == 0L) "Gratis" else formatClp(estimate.shipping))
                    if (s.delivery == Delivery.DOMICILIO && estimate.shipping > 0) Text(
                        "Despacho gratis desde ${formatClp(OrderEstimate.FREE_SHIPPING_FROM)}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    MMDivider()
                    TotalRow("Total estimado", formatClp(estimate.total), bold = true)
                    Text(
                        "El total final lo calcula Master Martini con el precio y stock vigentes.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            SegmentedChoice(
                "Pago simulado (proyecto académico, sin cobro real)",
                listOf(true, false), s.paymentApproved, vm::onPayment,
                { if (it) "Aprobar pago" else "Rechazar pago" },
            )
            s.formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            PrimaryButton(
                "Enviar pedido",
                onClick = { askNotifications { vm.submit() } },
                loading = s.loading,
                enabled = lines.isNotEmpty(),
                icon = Icons.AutoMirrored.Filled.Send,
                modifier = Modifier.shake(s.failedAttempts),
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    s.confirmingClass?.let { c ->
        AlertDialog(
            onDismissRequest = vm::dismissClass,
            title = { Text("Inscribirte en ${c.title}") },
            text = { Text(listOfNotNull(c.dateLabel, c.detail.takeIf { it.isNotBlank() }, "${c.availableSeats} cupos disponibles").joinToString("\n")) },
            confirmButton = { TextButton(onClick = vm::confirmClass) { Text("Confirmar inscripción") } },
            dismissButton = { TextButton(onClick = vm::dismissClass) { Text("Ahora no") } },
        )
    }

    s.sent?.let { sent ->
        val close = { if (sent.approved) onDone() else vm.dismissSent() }
        AlertDialog(
            onDismissRequest = close,
            icon = {
                if (sent.approved) Icon(Icons.Filled.CheckCircle, null, tint = LocalExtraColors.current.success)
                else Icon(Icons.Outlined.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
            },
            title = { Text(if (sent.approved) "Pedido ${sent.code} confirmado" else "Pago rechazado") },
            text = {
                Text(
                    if (sent.approved) {
                        "Total: ${formatClp(sent.total)}. Lo verás en Mi cuenta y también en la página web." +
                            if (sent.notified) " Te dejamos una notificación con el detalle." else ""
                    } else {
                        "El pedido ${sent.code} quedó registrado con el pago rechazado (simulado). " +
                            "No se descontó stock y tu cotización sigue guardada para intentarlo de nuevo."
                    }
                )
            },
            confirmButton = { TextButton(onClick = close) { Text(if (sent.approved) "Listo" else "Volver al pedido") } },
            dismissButton = {
                if (sent.approved) OutlinedButton(onClick = {
                    val text = "Pedido ${sent.code} (${formatClp(sent.total)}):\n${sent.summary}"
                    if (!ContactHelper.share(context, "Pedido ${sent.code}", text)) {
                        android.widget.Toast.makeText(context, "No hay app para compartir", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.AutoMirrored.Outlined.Chat, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Compartir resumen")
                }
            },
        )
    }
}

@Composable
private fun TotalRow(label: String, value: String, bold: Boolean = false) {
    val style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = style, modifier = Modifier.weight(1f))
        Text(value, style = style)
    }
}
