package com.equipo.sanmarkfood.comensal.presentation.reservas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.reservas.EstadoVisibleReserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.MotivoRechazoReserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.RechazoReserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva

@Immutable
data class ColoresEstado(val fondo: Color, val tinta: Color)

@Composable
fun coloresDe(estado: EstadoVisibleReserva): ColoresEstado {
    val colores = MaterialTheme.colorScheme
    return when (estado) {
        EstadoVisibleReserva.PENDIENTE -> ColoresEstado(colores.secondaryContainer, colores.onSecondaryContainer)
        EstadoVisibleReserva.CONFIRMADA, EstadoVisibleReserva.COMPLETADA ->
            ColoresEstado(colores.tertiaryContainer, colores.onTertiaryContainer)
        EstadoVisibleReserva.SIN_RESPUESTA, EstadoVisibleReserva.RECHAZADA ->
            ColoresEstado(colores.errorContainer, colores.onErrorContainer)
        EstadoVisibleReserva.CANCELADA -> ColoresEstado(colores.surfaceContainerHigh, colores.onSurfaceVariant)
    }
}

@Composable
fun textoEstado(estado: EstadoVisibleReserva): String = stringResource(
    when (estado) {
        EstadoVisibleReserva.PENDIENTE -> R.string.reserva_estado_corto_pendiente
        EstadoVisibleReserva.CONFIRMADA -> R.string.reserva_estado_confirmada
        EstadoVisibleReserva.COMPLETADA -> R.string.reserva_estado_completada
        EstadoVisibleReserva.SIN_RESPUESTA -> R.string.reserva_estado_sin_respuesta
        EstadoVisibleReserva.RECHAZADA -> R.string.reserva_estado_rechazada
        EstadoVisibleReserva.CANCELADA -> R.string.reserva_estado_cancelada
    }
)

@Composable
fun textoMotivo(rechazo: RechazoReserva?, estado: EstadoVisibleReserva): String? = when {
    estado == EstadoVisibleReserva.SIN_RESPUESTA -> stringResource(R.string.reserva_motivo_sin_respuesta)
    estado != EstadoVisibleReserva.RECHAZADA || rechazo == null -> null
    rechazo.motivo == MotivoRechazoReserva.OTRO -> rechazo.detalle ?: stringResource(R.string.reserva_motivo_otro)
    rechazo.motivo == MotivoRechazoReserva.SIN_MESAS -> stringResource(R.string.reserva_motivo_sin_mesas)
    rechazo.motivo == MotivoRechazoReserva.LOCAL_CERRADO -> stringResource(R.string.reserva_motivo_local_cerrado)
    rechazo.motivo == MotivoRechazoReserva.GRUPO_GRANDE -> stringResource(R.string.reserva_motivo_grupo_grande)
    else -> stringResource(R.string.reserva_motivo_sin_respuesta)
}

@Composable
fun TarjetaProximaReserva(
    item: ReservaEnLista,
    modifier: Modifier = Modifier,
    onModificar: (() -> Unit)? = null,
    onCancelar: (() -> Unit)? = null,
) {
    val reserva = item.reserva
    val forma = RoundedCornerShape(14.dp)
    val colores = coloresDe(item.estado)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.actividad_proxima_reserva),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                EtiquetaEstadoReserva(textoEstado(item.estado), colores.fondo, colores.tinta)
            }
            Text(
                text = "${fechaCorta(reserva.fecha)} · ${reserva.hora}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "${reserva.restauranteNombre} · ${etiquetaComensales(reserva.personas)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            item.recordatorio?.let {
                Text(
                    text = stringResource(R.string.actividad_recordatorio, textoAnticipacion(it)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (onModificar != null || onCancelar != null) {
            LineaPerforada(modifier = Modifier.padding(horizontal = 14.dp))
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onModificar != null) {
                    Button(
                        onClick = onModificar,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.inverseSurface,
                            contentColor = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    ) {
                        Text(stringResource(R.string.actividad_modificar), fontWeight = FontWeight.SemiBold)
                    }
                }
                if (onCancelar != null) {
                    OutlinedButton(
                        onClick = onCancelar,
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.actividad_cancelar), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun ListaReservas(
    reservas: List<ReservaEnLista>,
    modifier: Modifier = Modifier,
    onAbrir: ((Reserva) -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        reservas.forEachIndexed { indice, item ->
            if (indice > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            FilaReserva(item, onAbrir)
        }
    }
}

@Composable
private fun FilaReserva(item: ReservaEnLista, onAbrir: ((Reserva) -> Unit)?) {
    val reserva = item.reserva
    val colores = coloresDe(item.estado)
    val motivo = textoMotivo(reserva.rechazo, item.estado)
    val enlace = when (item.estado) {
        EstadoVisibleReserva.PENDIENTE, EstadoVisibleReserva.CONFIRMADA -> R.string.actividad_ver_detalle
        EstadoVisibleReserva.RECHAZADA, EstadoVisibleReserva.SIN_RESPUESTA -> R.string.actividad_ver_detalle_otro_horario
        else -> null
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = reserva.restauranteNombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "${fechaCorta(reserva.fecha)} · ${reserva.hora} · ${etiquetaComensales(reserva.personas)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (motivo != null) {
                Text(
                    text = stringResource(R.string.actividad_motivo, motivo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (enlace != null && onAbrir != null) {
                Text(
                    text = stringResource(enlace),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable(role = Role.Button) { onAbrir(reserva) }
                        .padding(vertical = 8.dp)
                )
            }
        }
        EtiquetaEstadoReserva(textoEstado(item.estado), colores.fondo, colores.tinta)
    }
}

@Composable
fun SinReservas(onVerLocales: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.actividad_sin_reservas_titulo),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.actividad_sin_reservas_texto),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(onClick = onVerLocales, modifier = Modifier.padding(top = 8.dp)) {
            Text(stringResource(R.string.actividad_ver_locales), fontWeight = FontWeight.SemiBold)
        }
    }
}
