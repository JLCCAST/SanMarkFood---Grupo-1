package com.equipo.sanmarkfood.comensal.presentation.pedidos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.perfil.Direccion

@Composable
fun SeccionDireccionEntrega(
    direcciones: List<Direccion>,
    elegida: Direccion?,
    onElegir: (Direccion) -> Unit,
    onAgregar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.pedido_direccion_titulo),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (direcciones.isEmpty()) {
            Text(
                text = stringResource(R.string.pedido_direccion_vacia),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        direcciones.forEach { direccion ->
            FilaDireccion(
                direccion = direccion,
                elegida = direccion == elegida,
                onElegir = { onElegir(direccion) }
            )
        }
        TextButton(onClick = onAgregar, contentPadding = PaddingValues(horizontal = 0.dp)) {
            Text(
                text = stringResource(R.string.pedido_direccion_agregar),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FilaDireccion(direccion: Direccion, elegida: Boolean, onElegir: () -> Unit) {
    val forma = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(
                width = 1.dp,
                color = if (elegida) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = forma
            )
            .selectable(selected = elegida, role = Role.RadioButton, onClick = onElegir)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = elegida, onClick = null, modifier = Modifier.padding(horizontal = 8.dp))
        Column {
            Text(
                text = direccion.etiqueta,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = direccion.direccionTexto,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DialogoNuevaDireccion(
    guardando: Boolean,
    error: Boolean,
    onGuardar: (etiqueta: String, texto: String) -> Unit,
    onCancelar: () -> Unit,
) {
    var etiqueta by rememberSaveable { mutableStateOf("") }
    var texto by rememberSaveable { mutableStateOf("") }
    val completa = etiqueta.isNotBlank() && texto.isNotBlank()

    AlertDialog(
        onDismissRequest = { if (!guardando) onCancelar() },
        title = { Text(stringResource(R.string.pedido_direccion_nueva_titulo)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = etiqueta,
                    onValueChange = { etiqueta = it },
                    label = { Text(stringResource(R.string.perfil_direccion_etiqueta)) },
                    placeholder = { Text(stringResource(R.string.perfil_direccion_placeholder_etiqueta)) },
                    singleLine = true,
                    enabled = !guardando,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    label = { Text(stringResource(R.string.perfil_direccion_etiqueta_direccion)) },
                    placeholder = { Text(stringResource(R.string.perfil_direccion_placeholder_direccion)) },
                    enabled = !guardando,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error) {
                    Text(
                        text = stringResource(R.string.error_perfil_direccion_guardar),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onGuardar(etiqueta, texto) }, enabled = completa && !guardando) {
                Text(stringResource(R.string.perfil_direccion_guardar))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar, enabled = !guardando) {
                Text(stringResource(R.string.pedido_direccion_cancelar))
            }
        }
    )
}
