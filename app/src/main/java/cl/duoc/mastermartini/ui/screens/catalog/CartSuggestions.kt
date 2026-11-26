package cl.duoc.mastermartini.ui.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.mastermartini.data.model.Product
import cl.duoc.mastermartini.data.model.SavedAddress
import cl.duoc.mastermartini.data.model.ServiceItem
import cl.duoc.mastermartini.data.model.formatClp
import cl.duoc.mastermartini.domain.ClassSuggestion
import cl.duoc.mastermartini.domain.ProductSuggestion
import cl.duoc.mastermartini.ui.components.CapsuleButton
import cl.duoc.mastermartini.ui.components.MMCard
import cl.duoc.mastermartini.ui.components.MMDivider
import cl.duoc.mastermartini.ui.components.ProductPhoto

/** "Completa tu pedido": productos sugeridos según el carrito, en una fila deslizable. */
@Composable
fun ProductSuggestionsRow(
    items: List<ProductSuggestion>,
    cartEmpty: Boolean,
    onAdd: (Product) -> Unit,
) {
    if (items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(Icons.Outlined.AutoAwesome, if (cartEmpty) "Te puede interesar" else "Completa tu pedido")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items, key = { it.product.id }) { s ->
                MMCard(Modifier.width(170.dp)) {
                    Column(Modifier.padding(10.dp)) {
                        ProductPhoto(s.product, Modifier.fillMaxWidth().height(96.dp).clip(MaterialTheme.shapes.medium))
                        Spacer(Modifier.height(8.dp))
                        Text(s.product.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(
                            s.reason, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(formatClp(s.product.finalPrice), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(6.dp))
                        CapsuleButton("Agregar", onClick = { onAdd(s.product) })
                    }
                }
            }
        }
    }
}

/** Clases de la página que aprovechan los productos del carrito. */
@Composable
fun ClassSuggestionsCard(items: List<ClassSuggestion>, onEnroll: (ServiceItem) -> Unit) {
    if (items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(Icons.Outlined.School, "Clases para sacarle partido")
        MMCard {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                items.forEachIndexed { i, s ->
                    if (i > 0) MMDivider()
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(s.service.title, style = MaterialTheme.typography.titleSmall)
                            Text(s.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Text(
                                listOfNotNull(s.service.dateLabel, s.service.badge.lowercase().replaceFirstChar { it.uppercase() }, "${s.service.availableSeats} cupos")
                                    .joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        CapsuleButton("Inscribirme", onClick = { onEnroll(s.service) })
                    }
                }
            }
        }
    }
}

/** Fila de direcciones guardadas para rellenar el despacho con un toque. */
@Composable
fun SavedAddressChips(addresses: List<SavedAddress>, selectedId: Long?, onPick: (SavedAddress) -> Unit) {
    if (addresses.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Mis direcciones", style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(addresses, key = { it.id }) { a ->
                FilterChip(
                    selected = a.id == selectedId,
                    onClick = { onPick(a) },
                    label = { Text("${a.label}: ${a.summary}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = { Icon(Icons.Outlined.Bookmark, null, Modifier.size(18.dp)) },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}
