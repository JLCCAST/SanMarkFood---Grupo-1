package com.equipo.sanmarkfood.comensal.presentation.reservas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.reservas.EstadoVisibleReserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.fechaLimaDe
import com.equipo.sanmarkfood.comensal.domain.model.reservas.horaLimaDe
import com.equipo.sanmarkfood.comensal.presentation.pedidos.DialogoErrorEnvio

private data class EventoReserva(val momento: Long?, val texto: String, val actual: Boolean)

@Composable
fun DetalleReservaScreen(
    onVolver: () -> Unit,
    onReservarOtra: (String) -> Unit,
    onVerLocales: () -> Unit,
    viewModel: DetalleReservaViewModel = hiltViewModel(),
) {
    val estado by viewModel.uiState.collectAsState()
    val reserva = estado.reserva

    if (estado.confirmandoCancelar && reserva != null) {
        DialogoCancelarReserva(
            reserva = reserva,
            cancelando = estado.cancelando,
            onVolver = viewModel::cerrarCancelar,
            onConfirmar = viewModel::confirmarCancelar
        )
    }
    estado.error?.let { DialogoErrorEnvio(mensajeDeErrorReserva(it), viewModel::cerrarError) }

    Scaffold(
        topBar = { BarraDetalle(onVolver) },
        bottomBar = {
            val visible = estado.estado
            if (reserva != null && visible != null) {
                AccionesDetalle(
                    reserva = reserva,
                    estado = visible,
                    onCancelar = viewModel::pedirCancelar,
                    onReservarOtra = { onReservarOtra(reserva.restauranteId) },
                    onVerLocales = onVerLocales
                )
            }
        }
    ) { padding ->
        val visible = estado.estado
        when {
            estado.cargando -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            estado.sinConexion -> MensajeDetalle(
                titulo = stringResource(R.string.envio_sin_conexion_titulo),
                texto = stringResource(R.string.detalle_reserva_sin_conexion),
                onReintentar = viewModel::reintentar,
                modifier = Modifier.padding(padding)
            )
            reserva == null || visible == null -> MensajeDetalle(
                titulo = stringResource(R.string.detalle_reserva_no_encontrada),
                texto = stringResource(R.string.detalle_reserva_no_encontrada_texto),
                onReintentar = null,
                modifier = Modifier.padding(padding)
            )
            else -> ContenidoDetalle(
                reserva = reserva,
                estado = visible,
                recordatorio = estado.recordatorio?.let { etiquetaRecordatorio(it) },
                ahora = estado.ahora,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun BarraDetalle(onVolver: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onVolver) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.detalle_reserva_volver)
            )
        }
        Text(
            text = stringResource(R.string.detalle_reserva_titulo),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ContenidoDetalle(
    reserva: Reserva,
    estado: EstadoVisibleReserva,
    recordatorio: String?,
    ahora: Long,
    modifier: Modifier = Modifier,
) {
    val colores = coloresDe(estado)
    val rechazada = estado == EstadoVisibleReserva.RECHAZADA || estado == EstadoVisibleReserva.SIN_RESPUESTA
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EtiquetaEstadoReserva(textoEtiquetaDetalle(estado), colores.fondo, colores.tinta)
        Text(
            text = tituloDetalle(estado, reserva.restauranteNombre),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        TicketReserva(
            titulo = reserva.restauranteNombre,
            filas = listOf(
                FilaTicket(stringResource(R.string.reserva_ticket_fecha), fechaLarga(reserva.fecha), rechazada),
                FilaTicket(stringResource(R.string.reserva_ticket_hora), reserva.hora, rechazada),
                FilaTicket(stringResource(R.string.reserva_ticket_comensales), reserva.personas.toString()),
            ),
            pie = when {
                estado == EstadoVisibleReserva.SIN_RESPUESTA ->
                    stringResource(R.string.detalle_reserva_que_paso, stringResource(R.string.detalle_reserva_sin_respuesta_motivo))
                rechazada -> textoMotivo(reserva.rechazo, estado)?.let {
                    stringResource(R.string.detalle_reserva_motivo_local, it.replaceFirstChar(Char::uppercase))
                }
                reserva.vigente -> recordatorio ?: stringResource(R.string.recordatorio_ninguno)
                else -> null
            }
        )
        NotaDetalle(reserva, estado)
        LineaDeTiempo(eventosDe(reserva, estado), ahora)
    }
}

@Composable
private fun textoEtiquetaDetalle(estado: EstadoVisibleReserva): String = when (estado) {
    EstadoVisibleReserva.PENDIENTE -> stringResource(R.string.reserva_estado_pendiente)
    EstadoVisibleReserva.RECHAZADA -> stringResource(R.string.detalle_reserva_rechazada_local)
    EstadoVisibleReserva.SIN_RESPUESTA -> stringResource(R.string.detalle_reserva_rechazada_auto)
    else -> textoEstado(estado)
}

@Composable
private fun tituloDetalle(estado: EstadoVisibleReserva, local: String): String = when (estado) {
    EstadoVisibleReserva.PENDIENTE -> stringResource(R.string.detalle_reserva_esperando, local)
    EstadoVisibleReserva.CONFIRMADA -> stringResource(R.string.detalle_reserva_confirmada, local)
    EstadoVisibleReserva.COMPLETADA -> stringResource(R.string.detalle_reserva_completada, local)
    EstadoVisibleReserva.RECHAZADA -> stringResource(R.string.detalle_reserva_rechazo_titulo, local)
    EstadoVisibleReserva.SIN_RESPUESTA -> stringResource(R.string.detalle_reserva_sin_respuesta_titulo)
    EstadoVisibleReserva.CANCELADA -> stringResource(R.string.detalle_reserva_cancelada_titulo)
}

@Composable
private fun NotaDetalle(reserva: Reserva, estado: EstadoVisibleReserva) {
    val texto = when (estado) {
        EstadoVisibleReserva.PENDIENTE -> {
            val limite = reserva.limiteDeRespuesta
            val aviso = limite?.let {
                stringResource(R.string.detalle_reserva_limite, diaYNumero(fechaLimaDe(it)), horaLimaDe(it))
            }
            listOfNotNull(aviso, stringResource(R.string.detalle_reserva_cuenta_activa)).joinToString("\n\n")
        }
        EstadoVisibleReserva.CONFIRMADA -> stringResource(R.string.detalle_reserva_cuenta_activa)
        EstadoVisibleReserva.RECHAZADA -> stringResource(R.string.detalle_reserva_rechazo_explica)
        EstadoVisibleReserva.SIN_RESPUESTA -> stringResource(R.string.detalle_reserva_sin_respuesta_explica)
        else -> null
    } ?: return
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    )
}

@Composable
private fun eventosDe(reserva: Reserva, estado: EstadoVisibleReserva): List<EventoReserva> {
    val eventos = mutableListOf(EventoReserva(reserva.creadoEn, stringResource(R.string.evento_reserva_enviada), false))
    reserva.horarioAnterior?.let {
        eventos += EventoReserva(
            reserva.actualizadoEn.takeIf { estado == EstadoVisibleReserva.PENDIENTE } ?: reserva.creadoEn,
            stringResource(R.string.evento_reserva_cambiada, "${fechaCorta(it.fecha)} · ${it.hora}"),
            false
        )
    }
    eventos += when (estado) {
        EstadoVisibleReserva.PENDIENTE ->
            EventoReserva(null, stringResource(R.string.evento_reserva_esperando), true)
        EstadoVisibleReserva.CONFIRMADA, EstadoVisibleReserva.COMPLETADA ->
            EventoReserva(reserva.respondidaEn, stringResource(R.string.evento_reserva_confirmada), false)
        EstadoVisibleReserva.RECHAZADA ->
            EventoReserva(reserva.respondidaEn, stringResource(R.string.evento_reserva_rechazada), false)
        EstadoVisibleReserva.SIN_RESPUESTA ->
            EventoReserva(reserva.limiteDeRespuesta, stringResource(R.string.evento_reserva_sin_respuesta), false)
        EstadoVisibleReserva.CANCELADA ->
            EventoReserva(reserva.actualizadoEn, stringResource(R.string.evento_reserva_cancelada), false)
    }
    return eventos
}

@Composable
private fun LineaDeTiempo(eventos: List<EventoReserva>, ahora: Long) {
    val hoy = fechaLimaDe(ahora)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        eventos.forEach { evento ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (evento.actual) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                        )
                )
                val momento = evento.momento
                Text(
                    text = when {
                        momento == null -> "—"
                        fechaLimaDe(momento) == hoy -> horaLimaDe(momento)
                        else -> "${fechaCorta(fechaLimaDe(momento))}, ${horaLimaDe(momento)}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(92.dp)
                )
                Text(
                    text = evento.texto,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (evento.actual) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AccionesDetalle(
    reserva: Reserva,
    estado: EstadoVisibleReserva,
    onCancelar: () -> Unit,
    onReservarOtra: () -> Unit,
    onVerLocales: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (estado) {
            EstadoVisibleReserva.PENDIENTE, EstadoVisibleReserva.CONFIRMADA -> {
                OutlinedButton(
                    onClick = onCancelar,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.actividad_cancelar), fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = onReservarOtra, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.detalle_reserva_otra), fontWeight = FontWeight.SemiBold)
                }
            }
            EstadoVisibleReserva.RECHAZADA, EstadoVisibleReserva.SIN_RESPUESTA -> {
                Button(
                    onClick = onReservarOtra,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.detalle_reserva_probar_otro), fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = onVerLocales, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.detalle_reserva_otros_locales), fontWeight = FontWeight.SemiBold)
                }
            }
            else -> if (reserva.restauranteId.isNotBlank()) {
                Button(
                    onClick = onReservarOtra,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.detalle_reserva_otra), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun MensajeDetalle(
    titulo: String,
    texto: String,
    onReintentar: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        Text(text = titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (onReintentar != null) {
            OutlinedButton(onClick = onReintentar) { Text(stringResource(R.string.envio_reintentar)) }
        }
    }
}
