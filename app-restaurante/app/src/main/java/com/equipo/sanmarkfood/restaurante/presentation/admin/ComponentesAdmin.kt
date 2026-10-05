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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.admin.ErrorAdmin
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.MotivoRechazo
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Rechazo
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.titulo
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.unirConY
import com.equipo.sanmarkfood.restaurante.ui.theme.extendedColors
import java.util.Calendar
import java.util.TimeZone

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
fun EtiquetaSolicitud(
    reenviado: Boolean,
    modifier: Modifier = Modifier,
    estado: EstadoRestaurante = EstadoRestaurante.PENDIENTE,
) {
    val colores = MaterialTheme.colorScheme
    val (texto, fondo, contenido) = when {
        estado == EstadoRestaurante.APROBADO ->
            Triple(R.string.solicitud_aprobado, colores.tertiaryContainer, colores.onTertiaryContainer)
        estado == EstadoRestaurante.RECHAZADO ->
            Triple(R.string.solicitud_rechazado, colores.errorContainer, colores.onErrorContainer)
        reenviado -> Triple(R.string.solicitud_reenviado, colores.primaryContainer, colores.onPrimaryContainer)
        else -> Triple(R.string.solicitud_nuevo, colores.secondaryContainer, colores.onSecondaryContainer)
    }
    Text(
        text = stringResource(texto),
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(fondo)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall,
        color = contenido,
    )
}

@Composable
fun fechaCorta(milisegundos: Long): String {
    val meses = stringArrayResource(R.array.meses_cortos)
    val fecha = Calendar.getInstance(TimeZone.getTimeZone("America/Lima")).apply { timeInMillis = milisegundos }
    return "${fecha.get(Calendar.DAY_OF_MONTH)} ${meses[fecha.get(Calendar.MONTH)]}"
}

@Composable
fun textoRechazo(rechazo: Rechazo): String {
    val detalle = rechazo.detalle
    val partes = rechazo.motivos.sortedBy { it.ordinal }.map { motivo ->
        if (motivo == MotivoRechazo.OTRO && detalle != null) detalle.trimEnd('.')
        else stringResource(motivo.etiquetaParaAdministrador())
    }
    return unirConY(partes)
}

@StringRes
fun MotivoRechazo.etiquetaParaAdministrador(): Int =
    if (this == MotivoRechazo.OTRO) R.string.revisar_rechazo_otro else titulo()

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
    ErrorAdmin.MotivoFaltante -> R.string.error_motivo_faltante
    ErrorAdmin.DetalleObligatorio -> R.string.error_detalle_obligatorio
    ErrorAdmin.YaRevisada -> R.string.error_ya_revisada
    ErrorAdmin.Desconocido -> R.string.error_desconocido
}
