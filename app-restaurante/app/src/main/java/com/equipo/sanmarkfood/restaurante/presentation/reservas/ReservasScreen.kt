package com.equipo.sanmarkfood.restaurante.presentation.reservas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CupoReservas
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.menu.diaSemanaDe
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.EstadoReserva
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.Franja
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.HorarioReserva
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.franjasDelDia
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.horaDelRechazo
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.nombre
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.nombreCorto
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme
import java.util.Locale

@Composable
fun ReservasScreen(
    horario: Horario,
    onEditarHorario: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReservasViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReservasContenido(
        uiState = uiState,
        horario = horario,
        onElegirDia = viewModel::onElegirDia,
        onReintentar = viewModel::onReintentar,
        onEditarHorario = onEditarHorario,
        modifier = modifier,
    )
}

@Composable
private fun ReservasContenido(
    uiState: ReservasUiState,
    horario: Horario,
    onElegirDia: (String) -> Unit,
    onReintentar: () -> Unit,
    onEditarHorario: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.reservas_titulo),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
            style = MaterialTheme.typography.displaySmall,
        )
        SelectorDias(
            dias = uiState.dias,
            diaElegido = uiState.diaElegido,
            tienePendientes = uiState::tienePendientes,
            onElegir = onElegirDia,
        )
        val error = uiState.error
        when {
            error != null -> Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(error.mensaje()),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                BotonPrincipal(texto = stringResource(R.string.arranque_reintentar), onClick = onReintentar)
            }
            uiState.cargando -> Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> ListaReservas(
                uiState = uiState,
                horario = horario,
                onEditarHorario = onEditarHorario,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SelectorDias(
    dias: List<String>,
    diaElegido: String,
    tienePendientes: (String) -> Boolean,
    onElegir: (String) -> Unit,
) {
    LazyRow(
        modifier = Modifier.padding(bottom = 6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(dias) { fecha ->
            ChipDia(
                fecha = fecha,
                hoy = fecha == dias.first(),
                elegido = fecha == diaElegido,
                conPendientes = tienePendientes(fecha),
                onClick = { onElegir(fecha) },
            )
        }
    }
}

@Composable
private fun ChipDia(fecha: String, hoy: Boolean, elegido: Boolean, conPendientes: Boolean, onClick: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    val forma = RoundedCornerShape(14.dp)
    val dia = diaSemanaDe(fecha)
    val numero = fecha.substring(8).toInt().toString()
    val etiqueta = if (hoy) stringResource(R.string.reservas_hoy) else stringResource(dia.nombreCorto()).uppercase(Locale.ROOT)
    val descripcion = buildString {
        append(stringResource(dia.nombre())).append(' ').append(numero)
        if (conPendientes) append(", ").append(stringResource(R.string.reservas_con_pendientes))
    }
    val texto = if (elegido) colores.inverseOnSurface else colores.onSurface
    Box(
        modifier = Modifier
            .size(width = 56.dp, height = 68.dp)
            .clip(forma)
            .border(1.5.dp, if (elegido) colores.inverseSurface else colores.outlineVariant, forma)
            .background(if (elegido) colores.inverseSurface else colores.surfaceContainerLowest)
            .selectable(selected = elegido, role = Role.Tab, onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = descripcion
                selected = elegido
                role = Role.Tab
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = etiqueta, style = MaterialTheme.typography.labelSmall, color = texto)
            Text(text = numero, style = MaterialTheme.typography.headlineSmall, color = texto)
        }
        if (conPendientes) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colores.error),
            )
        }
    }
}

@Composable
private fun ListaReservas(
    uiState: ReservasUiState,
    horario: Horario,
    onEditarHorario: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pendientes = uiState.pendientes
    val franjas = franjasDelDia(horario.dias[diaSemanaDe(uiState.diaElegido)], uiState.delDia)
    val cupo = horario.reservas.porFranja
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (uiState.sinConexion) {
            item { AvisoSinConexion() }
        }
        item { TituloSeccion(stringResource(R.string.reservas_por_responder, pendientes.size)) }
        if (pendientes.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.reservas_sin_pendientes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(pendientes, key = { it.id }) { reserva ->
            val confirmados = franjas.firstOrNull { it.hora == reserva.hora }?.confirmados ?: 0
            TarjetaReserva(reserva = reserva, ocupacion = confirmados + reserva.personas, cupo = cupo)
        }
        item {
            CabeceraFranjas(
                reservas = horario.reservas,
                onEditarHorario = onEditarHorario,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (franjas.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.reservas_dia_cerrado),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(franjas, key = { "franja-${it.hora}" }) { franja ->
            FilaFranja(franja = franja, cupo = cupo)
        }
        item {
            Text(
                text = stringResource(R.string.reservas_leyenda),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TituloSeccion(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun AvisoSinConexion() {
    Text(
        text = stringResource(R.string.reservas_sin_conexion),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onErrorContainer,
    )
}

@Composable
private fun TarjetaReserva(reserva: Reserva, ocupacion: Int, cupo: Int) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .border(1.5.dp, MaterialTheme.colorScheme.secondary, forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = pluralStringResource(R.plurals.reservas_hora_personas, reserva.personas, reserva.hora, reserva.personas),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = reserva.comensalNombre,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        reserva.anterior?.let { anterior ->
            Text(
                text = textoDelCambio(anterior, reserva),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Text(
            text = stringResource(R.string.reservas_franja_queda, ocupacion, cupo),
            style = MaterialTheme.typography.bodySmall,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_reloj),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.reservas_se_rechaza, horaDelRechazo(reserva.hora)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun textoDelCambio(anterior: HorarioReserva, reserva: Reserva): String =
    if (anterior.fecha == reserva.fecha) {
        stringResource(R.string.reservas_cambio_hora, anterior.hora, reserva.hora)
    } else {
        val dia = stringResource(diaSemanaDe(anterior.fecha).nombreCorto())
        stringResource(R.string.reservas_cambio_dia, "$dia ${anterior.fecha.substring(8).toInt()}", anterior.hora)
    }

@Composable
private fun CabeceraFranjas(reservas: CupoReservas, onEditarHorario: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TituloSeccion(stringResource(R.string.reservas_confirmadas_por_franja), Modifier.weight(1f))
        TextButton(onClick = onEditarHorario) {
            Text(
                text = if (reservas.acepta) {
                    stringResource(R.string.reservas_cupo_cambiar, reservas.porFranja)
                } else {
                    stringResource(R.string.reservas_apagadas_cambiar)
                },
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun FilaFranja(franja: Franja, cupo: Int) {
    val llena = franja.llena(cupo)
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(text = franja.hora, modifier = Modifier.width(48.dp), style = MaterialTheme.typography.titleSmall)
                BarraFranja(franja = franja, cupo = cupo, llena = llena, modifier = Modifier.weight(1f))
                Text(
                    text = if (llena) {
                        stringResource(R.string.reservas_lleno)
                    } else {
                        stringResource(R.string.reservas_de_cupo, franja.confirmados + franja.pendientes, cupo)
                    },
                    modifier = Modifier.widthIn(min = 72.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (llena) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                )
            }
            if (franja.confirmadas.isNotEmpty()) {
                Text(
                    text = franja.confirmadas.joinToString(" · ") { "${it.comensalNombre} (${it.personas})" },
                    modifier = Modifier.padding(start = 58.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun BarraFranja(franja: Franja, cupo: Int, llena: Boolean, modifier: Modifier = Modifier) {
    val colores = MaterialTheme.colorScheme
    val confirmados = franja.confirmados.coerceAtMost(cupo)
    val pendientes = franja.pendientes.coerceAtMost(cupo - confirmados)
    val libres = cupo - confirmados - pendientes
    val descripcion = stringResource(
        R.string.reservas_franja_descripcion,
        franja.hora,
        franja.confirmados,
        franja.pendientes,
        cupo,
    )
    Row(
        modifier = modifier
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(colores.surfaceContainer)
            .clearAndSetSemantics {
                contentDescription = descripcion
                role = Role.Image
            },
    ) {
        if (confirmados > 0) {
            Box(
                Modifier
                    .weight(confirmados.toFloat())
                    .fillMaxHeight()
                    .background(if (llena) colores.primary else colores.tertiary)
            )
        }
        if (pendientes > 0) {
            Box(Modifier.weight(pendientes.toFloat()).fillMaxHeight().background(colores.secondary))
        }
        if (libres > 0) {
            Spacer(Modifier.weight(libres.toFloat()))
        }
    }
}

private fun ErrorReservas.mensaje(): Int = when (this) {
    ErrorReservas.SinConexion -> R.string.error_sin_conexion
    ErrorReservas.Desconocido -> R.string.error_desconocido
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ReservasPreview() {
    val hoy = "2026-11-09"
    val reservas = listOf(
        Reserva("1", "Rocío T.", hoy, "13:30", 4, EstadoReserva.PENDIENTE, HorarioReserva(hoy, "12:30")),
        Reserva("2", "Kevin R.", hoy, "14:00", 10, EstadoReserva.PENDIENTE, null),
        Reserva("3", "Luis M.", hoy, "12:00", 4, EstadoReserva.CONFIRMADA, null),
        Reserva("4", "Grupo Letras", hoy, "13:00", 12, EstadoReserva.CONFIRMADA, null),
    )
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ReservasContenido(
                uiState = ReservasUiState(
                    cargando = false,
                    dias = List(7) { "2026-11-${(9 + it).toString().padStart(2, '0')}" },
                    diaElegido = hoy,
                    reservas = reservas,
                ),
                horario = Horario.PorDefecto,
                onElegirDia = {},
                onReintentar = {},
                onEditarHorario = {},
            )
        }
    }
}
