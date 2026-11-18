package com.equipo.sanmarkfood.comensal.presentation.reservas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.equipo.sanmarkfood.comensal.domain.model.reservas.EstadoReserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.comensal.presentation.pedidos.DialogoErrorEnvio
import com.equipo.sanmarkfood.comensal.presentation.pedidos.DialogoSinConexion
import com.equipo.sanmarkfood.comensal.domain.model.reservas.ErrorReservas

@Composable
fun ModificarReservaScreen(
    onVolver: () -> Unit,
    onVolverAActividad: () -> Unit,
    viewModel: ModificarReservaViewModel = hiltViewModel(),
) {
    val estado by viewModel.uiState.collectAsState()
    val reserva = estado.reserva

    estado.guardado?.let { HojaReservaCambiada(it, onVolverAActividad) }

    if (estado.confirmando && reserva != null) {
        val fecha = estado.fecha
        val hora = estado.horaElegida
        if (fecha != null && hora != null) {
            AlertDialog(
                onDismissRequest = viewModel::cerrarConfirmacion,
                title = { Text(stringResource(R.string.modificar_confirmar_titulo)) },
                text = {
                    Text(
                        stringResource(
                            R.string.modificar_confirmar_texto,
                            diaYNumero(reserva.fecha),
                            reserva.hora,
                            resumenCorto(fecha, hora, reserva.personas)
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = viewModel::guardar) {
                        Text(stringResource(R.string.modificar_confirmar_boton), fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::cerrarConfirmacion) { Text(stringResource(R.string.cancelar_volver)) }
                }
            )
        }
    }

    when (val error = estado.error) {
        null -> Unit
        ErrorReservas.SinConexion -> DialogoSinConexion(
            onAhoraNo = viewModel::cerrarError,
            onReintentar = {
                viewModel.cerrarError()
                viewModel.guardar()
            }
        )
        else -> DialogoErrorEnvio(mensaje = mensajeDeErrorReserva(error), onCerrar = viewModel::cerrarError)
    }

    Scaffold(
        topBar = { BarraModificar(estado.restaurante?.nombre ?: reserva?.restauranteNombre.orEmpty(), onVolver) },
        bottomBar = {
            if (reserva != null && estado.restaurante != null) {
                PieGuardar(estado, viewModel::pedirConfirmacion)
            }
        }
    ) { padding ->
        when {
            estado.sinConexion -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
            ) {
                Text(
                    text = stringResource(R.string.envio_sin_conexion_titulo),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.reserva_sin_conexion_mensaje),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                OutlinedButton(onClick = viewModel::reintentar) { Text(stringResource(R.string.envio_reintentar)) }
            }
            reserva == null || estado.restaurante == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(26.dp)
            ) {
                ReservaActual(reserva)
                SeccionFecha(estado.dias, estado.fecha, viewModel::elegirFecha, actual = reserva.fecha)
                SeccionHora(estado.horarios, estado.hora, estado.primeraHoraDeHoy, viewModel::elegirHora)
                Text(
                    text = stringResource(R.string.modificar_nota),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun resumenCorto(fecha: String, hora: String, personas: Int): String =
    "${fechaCorta(fecha).substringBeforeLast(' ')} · $hora · $personas"

@Composable
private fun BarraModificar(nombreLocal: String, onVolver: () -> Unit) {
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
                contentDescription = stringResource(R.string.modificar_volver)
            )
        }
        Column {
            Text(
                text = stringResource(R.string.modificar_titulo),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (nombreLocal.isNotBlank()) {
                Text(
                    text = nombreLocal,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReservaActual(reserva: Reserva) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = stringResource(
                if (reserva.estado == EstadoReserva.CONFIRMADA) R.string.modificar_actual_confirmada
                else R.string.modificar_actual_pendiente
            ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${fechaCorta(reserva.fecha)} · ${reserva.hora} · ${etiquetaComensales(reserva.personas)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PieGuardar(estado: ModificarReservaUiState, onGuardar: () -> Unit) {
    val reserva = estado.reserva ?: return
    Column(modifier = Modifier.navigationBarsPadding()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        if (estado.sinCambios) R.string.modificar_sin_cambios else R.string.modificar_nuevo_horario
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val fecha = estado.fecha
                val hora = if (estado.sinCambios) reserva.hora else estado.horaElegida
                Text(
                    text = if (fecha != null && hora != null) {
                        "${fechaCorta(fecha)} · $hora · ${etiquetaComensales(reserva.personas)}"
                    } else {
                        stringResource(R.string.reserva_elige_horario)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = onGuardar,
                enabled = estado.puedeGuardar,
                modifier = Modifier.height(52.dp),
                contentPadding = PaddingValues(horizontal = 22.dp)
            ) {
                if (estado.guardando) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(stringResource(R.string.modificar_guardar), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaReservaCambiada(cambio: CambioGuardado, onVolverAActividad: () -> Unit) {
    val anterior = cambio.anterior
    ModalBottomSheet(
        onDismissRequest = onVolverAActividad,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            EtiquetaEstadoReserva(
                texto = stringResource(R.string.reserva_estado_pendiente),
                fondo = MaterialTheme.colorScheme.secondaryContainer,
                tinta = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = stringResource(R.string.modificar_hecho_titulo),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            TicketReserva(
                filas = listOf(
                    FilaTicket(
                        stringResource(R.string.modificar_liberada),
                        resumenCorto(anterior.fecha, anterior.hora, anterior.personas),
                        tachado = true
                    ),
                    FilaTicket(
                        stringResource(R.string.modificar_nueva),
                        resumenCorto(cambio.fecha, cambio.hora, anterior.personas)
                    ),
                ),
                pie = etiquetaRecordatorio(cambio.recordatorio)
            )
            Text(
                text = stringResource(R.string.modificar_hecho_texto),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onVolverAActividad,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(stringResource(R.string.modificar_volver_actividad), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
