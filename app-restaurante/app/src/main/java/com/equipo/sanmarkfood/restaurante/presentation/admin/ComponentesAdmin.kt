package com.equipo.sanmarkfood.restaurante.presentation.admin

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.admin.ErrorAdmin
import com.equipo.sanmarkfood.restaurante.ui.theme.extendedColors

@Composable
fun CabeceraAdministracion(
    titulo: String,
    subtitulo: String,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colores = MaterialTheme.extendedColors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colores.pizarra)
            .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.admin_etiqueta),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = onSalir,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
            ) {
                Text(text = stringResource(R.string.admin_salir), style = MaterialTheme.typography.labelLarge)
            }
        }
        Column(
            modifier = Modifier.padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = titulo,
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.displaySmall,
                color = colores.sobreSuperficieFija,
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = colores.sobreSuperficieFijaTenue,
            )
        }
    }
}

@Composable
fun MensajeErrorAdmin(error: ErrorAdmin, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(error.mensaje()),
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
    )
}

@StringRes
private fun ErrorAdmin.mensaje(): Int = when (this) {
    ErrorAdmin.SinConexion -> R.string.error_sin_conexion
    ErrorAdmin.Desconocido -> R.string.error_desconocido
}
