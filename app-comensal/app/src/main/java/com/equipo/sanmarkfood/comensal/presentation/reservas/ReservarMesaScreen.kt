package com.equipo.sanmarkfood.comensal.presentation.reservas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.reservas.DiaReservable
import com.equipo.sanmarkfood.comensal.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.comensal.domain.model.reservas.HorarioReservable
import com.equipo.sanmarkfood.comensal.domain.model.reservas.MAX_COMENSALES
import com.equipo.sanmarkfood.comensal.domain.model.reservas.MIN_COMENSALES
import com.equipo.sanmarkfood.comensal.presentation.pedidos.DialogoErrorEnvio
import com.equipo.sanmarkfood.comensal.presentation.pedidos.DialogoSinConexion

private const val HORAS_POR_FILA = 4

@Composable
fun ReservarMesaScreen(
    onVolver: () -> Unit,
    onVerActividad: () -> Unit,
    viewModel: ReservarMesaViewModel = hiltViewModel(),
) {
    val estado by viewModel.uiState.collectAsState()

    estado.enviada?.let { enviada ->
        HojaReservaEnviada(
            enviada = enviada,
            pie = null,
            onListo = onVolver,
            onVerActividad = onVerActividad
        )
    }

    when (val error = estado.error) {
        null -> Unit
        ErrorReservas.SinConexion -> DialogoSinConexion(
            onAhoraNo = viewModel::cerrarError,
            onReintentar = {
                viewModel.cerrarError()
                viewModel.solicitar()
            }
        )
        else -> DialogoErrorEnvio(mensaje = mensajeDeErrorReserva(error), onCerrar = viewModel::cerrarError)
    }

    Scaffold(
        topBar = { BarraReservar(estado.restaurante?.nombre.orEmpty(), onVolver) },
        bottomBar = {
            if (estado.restaurante != null) {
                PieSolicitar(estado = estado, onSolicitar = viewModel::solicitar)
            }
        }
    ) { padding ->
        val restaurante = estado.restaurante
        when {
            estado.sinConexion -> SinConexionReservar(viewModel::reintentarCarga, Modifier.padding(padding))
            restaurante == null -> Box(
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
                if (estado.alcanzoElTope) AvisoTopeReservas(onVerActividad)
                SeccionComensales(
                    personas = estado.personas,
                    telefono = restaurante.telefono,
                    onMenos = viewModel::menosComensales,
                    onMas = viewModel::masComensales
                )
                SeccionFecha(estado.dias, estado.fecha, viewModel::elegirFecha)
                SeccionHora(estado.horarios, estado.hora, estado.primeraHoraDeHoy, viewModel::elegirHora)
                Text(
                    text = stringResource(R.string.reserva_nota),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BarraReservar(nombreLocal: String, onVolver: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(onClick = onVolver) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.detalle_volver)
            )
        }
        Column {
            Text(
                text = stringResource(R.string.reserva_titulo),
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
private fun AvisoTopeReservas(onVerActividad: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.reserva_tope_titulo),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
        Text(
            text = stringResource(R.string.reserva_tope_mensaje),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
        Text(
            text = stringResource(R.string.reserva_tope_ver),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier
                .clickable(role = Role.Button, onClick = onVerActividad)
                .padding(vertical = 10.dp)
        )
    }
}

@Composable
private fun SeccionComensales(
    personas: Int,
    telefono: String,
    onMenos: () -> Unit,
    onMas: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.reserva_comensales_titulo),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.reserva_comensales_rango, MIN_COMENSALES, MAX_COMENSALES),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BotonCantidad(
                texto = "−",
                descripcion = stringResource(R.string.reserva_comensales_menos),
                relleno = false,
                habilitado = personas > MIN_COMENSALES,
                onClick = onMenos
            )
            Text(
                text = personas.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp)
            )
            BotonCantidad(
                texto = "+",
                descripcion = stringResource(R.string.reserva_comensales_mas),
                relleno = true,
                habilitado = personas < MAX_COMENSALES,
                onClick = onMas
            )
        }
        if (personas >= MAX_COMENSALES) {
            Text(
                text = stringResource(
                    R.string.reserva_grupo_grande,
                    telefono.ifBlank { stringResource(R.string.reserva_telefono_desconocido) }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun BotonCantidad(
    texto: String,
    descripcion: String,
    relleno: Boolean,
    habilitado: Boolean,
    onClick: () -> Unit,
) {
    val tinta = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .size(44.dp)
            .alpha(if (habilitado) 1f else 0.4f)
            .clip(CircleShape)
            .then(
                if (relleno) Modifier.background(MaterialTheme.colorScheme.inverseSurface)
                else Modifier.border(1.5.dp, tinta, CircleShape)
            )
            .clickable(enabled = habilitado, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (relleno) MaterialTheme.colorScheme.inverseOnSurface else tinta
        )
    }
}

@Composable
private fun EncabezadoSeccion(titulo: String, detalle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(text = titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            text = detalle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SeccionFecha(dias: List<DiaReservable>, fecha: String?, onElegir: (String) -> Unit, actual: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        EncabezadoSeccion(stringResource(R.string.reserva_fecha_titulo), stringResource(R.string.reserva_fecha_detalle))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(dias, key = { it.fecha }) { dia ->
                ChipDia(
                    dia = dia,
                    seleccionado = dia.fecha == fecha,
                    nota = when {
                        dia.fecha == actual -> stringResource(R.string.reserva_dia_actual)
                        dia.cerrado -> stringResource(R.string.reserva_dia_cerrado)
                        dia.esHoy -> stringResource(R.string.reserva_dia_hoy)
                        else -> ""
                    },
                    onClick = { onElegir(dia.fecha) }
                )
            }
        }
    }
}

@Composable
private fun ChipDia(dia: DiaReservable, seleccionado: Boolean, nota: String, onClick: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    val forma = RoundedCornerShape(14.dp)
    val habilitado = dia.disponible
    val fondo = when {
        seleccionado -> colores.inverseSurface
        !habilitado -> colores.surfaceContainerHigh
        else -> colores.surfaceContainerLowest
    }
    val borde = when {
        seleccionado -> colores.inverseSurface
        !habilitado -> colores.surfaceContainerHigh
        else -> colores.outlineVariant
    }
    val tinta = when {
        seleccionado -> colores.inverseOnSurface
        !habilitado -> colores.onSurfaceVariant
        else -> colores.onSurface
    }
    Column(
        modifier = Modifier
            .width(58.dp)
            .height(78.dp)
            .clip(forma)
            .background(fondo)
            .border(1.5.dp, borde, forma)
            .clickable(enabled = habilitado, role = Role.Button, onClick = onClick)
            .semantics { selected = seleccionado },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = diaCorto(dia.claveDia),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = tinta
        )
        Text(
            text = dia.diaDelMes.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = tinta
        )
        Text(
            text = nota,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = tinta
        )
    }
}

@Composable
fun SeccionHora(
    horarios: List<HorarioReservable>,
    hora: String?,
    primeraHoraDeHoy: String?,
    onElegir: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        EncabezadoSeccion(stringResource(R.string.reserva_hora_titulo), stringResource(R.string.reserva_hora_detalle))
        if (primeraHoraDeHoy != null) {
            Text(
                text = stringResource(R.string.reserva_hoy_desde, primeraHoraDeHoy),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (horarios.isEmpty()) {
            Text(
                text = stringResource(R.string.reserva_sin_horarios),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        horarios.chunked(HORAS_POR_FILA).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fila.forEach { horario ->
                    ChipHora(
                        horario = horario,
                        seleccionado = horario.hora == hora && !horario.lleno,
                        onClick = { onElegir(horario.hora) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(HORAS_POR_FILA - fila.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ChipHora(
    horario: HorarioReservable,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colores = MaterialTheme.colorScheme
    val forma = RoundedCornerShape(10.dp)
    val fondo = when {
        seleccionado -> colores.primary
        horario.lleno -> colores.surfaceContainerHigh
        else -> colores.surface
    }
    val borde = when {
        seleccionado -> colores.primary
        horario.lleno -> colores.surfaceContainerHigh
        else -> colores.outline
    }
    val tinta = when {
        seleccionado -> colores.onPrimary
        horario.lleno -> colores.onSurfaceVariant
        else -> colores.onSurface
    }
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(forma)
            .background(fondo)
            .border(1.5.dp, borde, forma)
            .clickable(enabled = !horario.lleno, role = Role.Button, onClick = onClick)
            .semantics { selected = seleccionado },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = horario.hora,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = tinta,
            textDecoration = if (horario.lleno) TextDecoration.LineThrough else TextDecoration.None
        )
    }
}

@Composable
private fun PieSolicitar(estado: ReservarMesaUiState, onSolicitar: () -> Unit) {
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
                val fecha = estado.fecha
                val hora = estado.horaElegida
                Text(
                    text = when {
                        fecha != null && hora != null -> "${fechaCorta(fecha)} · $hora"
                        else -> stringResource(R.string.reserva_elige_horario)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = etiquetaComensales(estado.personas),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onSolicitar,
                enabled = estado.puedeSolicitar,
                modifier = Modifier.height(52.dp),
                contentPadding = PaddingValues(horizontal = 22.dp)
            ) {
                if (estado.enviando) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(stringResource(R.string.reserva_solicitar), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SinConexionReservar(onReintentar: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_sin_conexion),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.envio_sin_conexion_titulo),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.reserva_sin_conexion_mensaje),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onReintentar) { Text(stringResource(R.string.envio_reintentar)) }
    }
}
