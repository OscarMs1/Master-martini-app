package cl.duoc.mastermartini.ui.components

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import cl.duoc.mastermartini.domain.validation.FieldState

/** Campo con lista desplegable que filtra mientras se escribe (región, comuna). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoiceField(
    label: String,
    field: FieldState,
    options: List<String>,
    onChange: (String) -> Unit,
    leadingIcon: ImageVector,
    placeholder: String = "Escribe o elige",
) {
    var expanded by remember { mutableStateOf(false) }
    val exact = options.any { it.equals(field.value.trim(), ignoreCase = true) }
    val shown = if (field.value.isBlank() || exact) options
    else options.filter { it.contains(field.value.trim(), ignoreCase = true) }
    ExposedDropdownMenuBox(expanded = expanded && shown.isNotEmpty(), onExpandedChange = { expanded = it }) {
        MMTextField(
            label = label,
            field = field,
            // Al escribir una opción completa se cierra la lista.
            onValueChange = { v -> onChange(v); expanded = options.none { it.equals(v.trim(), ignoreCase = true) } },
            leadingIcon = leadingIcon,
            placeholder = placeholder,
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true),
            trailingAction = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(expanded = expanded && shown.isNotEmpty(), onDismissRequest = { expanded = false }) {
            shown.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onChange(option); expanded = false })
            }
        }
    }
}
