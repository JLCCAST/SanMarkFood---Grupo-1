package com.equipo.sanmarkfood.comensal.presentation.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.presentation.pedidos.DialogoErrorEnvio
import com.equipo.sanmarkfood.comensal.presentation.reservas.DialogoCancelarReserva
import com.equipo.sanmarkfood.comensal.presentation.reservas.ListaReservas
import com.equipo.sanmarkfood.comensal.presentation.reservas.ReservasActividadUiState
import com.equipo.sanmarkfood.comensal.presentation.reservas.ReservasActividadViewModel
import com.equipo.sanmarkfood.comensal.presentation.reservas.SinReservas
import com.equipo.sanmarkfood.comensal.presentation.reservas.TarjetaProximaReserva
import com.equipo.sanmarkfood.comensal.presentation.reservas.mensajeDeErrorReserva

enum class PestanaActividad(val titulo: Int) {
    PEDIDOS(R.string.actividad_tab_pedidos),
    RESERVAS(R.string.actividad_tab_reservas),
}

@Composable
fun ActividadScreen(
    abrirReservas: Boolean,
    onReservasAbiertas: () -> Unit,
    onVerLocales: () -> Unit,
    onAbrirReserva: (String) -> Unit,
    onModificarReserva: (String) -> Unit,
    modifier: Modifier = Modifier,
    reservasViewModel: ReservasActividadViewModel = hiltViewModel(),
) {
    val reservas by reservasViewModel.uiState.collectAsState()
    var pestana by rememberSaveable { mutableIntStateOf(PestanaActividad.PEDIDOS.ordinal) }

    reservas.porCancelar?.let { reserva ->
        DialogoCancelarReserva(
            reserva = reserva,
            cancelando = reservas.cancelando,
            onVolver = reservasViewModel::cerrarCancelar,
            onConfirmar = reservasViewModel::confirmarCancelar
        )
    }
    reservas.error?.let { DialogoErrorEnvio(mensajeDeErrorReserva(it), reservasViewModel::cerrarError) }

    LaunchedEffect(abrirReservas) {
        if (abrirReservas) {
            pestana = PestanaActividad.RESERVAS.ordinal
            onReservasAbiertas()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.home_tab_actividad),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
        )
        reservas.proxima?.let { proxima ->
            TarjetaProximaReserva(
                item = proxima,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp),
                onModificar = { onModificarReserva(proxima.reserva.id) },
                onCancelar = { reservasViewModel.pedirCancelar(proxima.reserva) }
            )
        }
        PrimaryTabRow(
            selectedTabIndex = pestana,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(top = 18.dp)
        ) {
            PestanaActividad.entries.forEach { opcion ->
                Tab(
                    selected = pestana == opcion.ordinal,
                    onClick = { pestana = opcion.ordinal },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = { Text(stringResource(opcion.titulo), fontWeight = FontWeight.Bold) }
                )
            }
        }
        when (PestanaActividad.entries[pestana]) {
            PestanaActividad.PEDIDOS -> Text(
                text = stringResource(R.string.actividad_pedidos_pronto),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 40.dp)
            )
            PestanaActividad.RESERVAS -> ContenidoReservas(reservas, reservasViewModel::reintentar, onVerLocales, onAbrirReserva)
        }
    }
}

@Composable
private fun ContenidoReservas(
    estado: ReservasActividadUiState,
    onReintentar: () -> Unit,
    onVerLocales: () -> Unit,
    onAbrirReserva: (String) -> Unit,
) {
    when {
        estado.cargando -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        estado.sinConexion -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.envio_sin_conexion_titulo),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.actividad_sin_conexion),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            OutlinedButton(onClick = onReintentar) { Text(stringResource(R.string.envio_reintentar)) }
        }
        estado.reservas.isEmpty() -> SinReservas(onVerLocales)
        else -> ListaReservas(
            reservas = estado.reservas,
            modifier = Modifier.padding(horizontal = 16.dp),
            onAbrir = { onAbrirReserva(it.id) }
        )
    }
}
