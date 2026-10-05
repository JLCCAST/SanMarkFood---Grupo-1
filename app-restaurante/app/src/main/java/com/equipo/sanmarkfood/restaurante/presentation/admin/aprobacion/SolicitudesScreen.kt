package com.equipo.sanmarkfood.restaurante.presentation.admin.aprobacion

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.admin.FiltroSolicitudes
import com.equipo.sanmarkfood.restaurante.domain.model.admin.SolicitudLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.MotivoRechazo
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Rechazo
import com.equipo.sanmarkfood.restaurante.presentation.admin.CabeceraAdministracion
import com.equipo.sanmarkfood.restaurante.presentation.admin.EtiquetaSolicitud
import com.equipo.sanmarkfood.restaurante.presentation.admin.MensajeErrorAdmin
import com.equipo.sanmarkfood.restaurante.presentation.admin.fechaCorta
import com.equipo.sanmarkfood.restaurante.presentation.admin.textoRechazo
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.LogoLocal
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.etiqueta
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme
import com.equipo.sanmarkfood.restaurante.ui.theme.extendedColors
import java.util.Locale

@Composable
fun SolicitudesScreen(
    onSalir: () -> Unit,
    onAbrirSolicitud: (uid: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SolicitudesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SolicitudesContenido(
        uiState = uiState,
        onSalir = onSalir,
        onElegirFiltro = viewModel::onElegirFiltro,
        onAbrirSolicitud = onAbrirSolicitud,
        onReintentar = viewModel::onReintentar,
        modifier = modifier,
    )
}

@Composable
private fun SolicitudesContenido(
    uiState: SolicitudesUiState,
    onSalir: () -> Unit,
    onElegirFiltro: (FiltroSolicitudes) -> Unit,
    onAbrirSolicitud: (uid: String) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        CabeceraAdministracion(
            titulo = stringResource(R.string.solicitudes_titulo),
            subtitulo = stringResource(R.string.solicitudes_subtitulo),
            onSalir = onSalir,
        )
        FiltrosSolicitudes(
            actual = uiState.filtro,
            pendientes = uiState.cantidadPendientes,
            onElegir = onElegirFiltro,
        )

        val lista = uiState.lista
        val error = lista.error
        when {
            lista.cargando -> Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            error != null -> Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MensajeErrorAdmin(error = error)
                BotonPrincipal(texto = stringResource(R.string.arranque_reintentar), onClick = onReintentar)
            }

            lista.solicitudes.isEmpty() -> Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(uiState.filtro.tituloVacio()),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(uiState.filtro.textoVacio()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            else -> {
                val ahora = remember(lista.solicitudes) { System.currentTimeMillis() }
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(lista.solicitudes, key = { it.uid }) { solicitud ->
                        TarjetaSolicitud(
                            solicitud = solicitud,
                            ahora = ahora,
                            onAbrir = { onAbrirSolicitud(solicitud.uid) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltrosSolicitudes(actual: FiltroSolicitudes, pendientes: Int?, onElegir: (FiltroSolicitudes) -> Unit) {
    val colores = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FiltroSolicitudes.entries.forEach { filtro ->
            val seleccionado = filtro == actual
            FilterChip(
                selected = seleccionado,
                onClick = { onElegir(filtro) },
                label = {
                    Text(
                        text = if (filtro == FiltroSolicitudes.PENDIENTES && pendientes != null) {
                            stringResource(R.string.filtro_pendientes_cantidad, pendientes)
                        } else {
                            stringResource(filtro.etiqueta())
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                modifier = Modifier.height(36.dp),
                colors = FilterChipDefaults.filterChipColors(
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    selectedContainerColor = colores.pizarra,
                    selectedLabelColor = colores.sobreSuperficieFija,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = seleccionado,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = colores.pizarra,
                ),
            )
        }
    }
}

@Composable
private fun TarjetaSolicitud(solicitud: SolicitudLocal, ahora: Long, onAbrir: () -> Unit) {
    val forma = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
            .clickable(onClick = onAbrir)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LogoLocal(logoUrl = solicitud.logoUrl, nombre = solicitud.nombre, tamano = 48.dp, radio = 12.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = solicitud.nombre,
                    modifier = Modifier.weight(1f, fill = false),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                EtiquetaSolicitud(reenviado = solicitud.reenviado, estado = solicitud.estado)
            }
            Text(
                text = detalleSolicitud(solicitud, ahora),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val rechazoAnterior = solicitud.rechazoAnterior
            if (solicitud.estado == EstadoRestaurante.PENDIENTE && rechazoAnterior != null) {
                Text(
                    text = stringResource(R.string.solicitud_rechazo_anterior, motivoAnterior(rechazoAnterior)),
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
        Icon(
            painter = painterResource(R.drawable.ic_siguiente),
            contentDescription = null,
            modifier = Modifier.padding(top = 14.dp).size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun detalleSolicitud(solicitud: SolicitudLocal, ahora: Long): String {
    val platos = solicitud.cantidadPlatos
    return listOfNotNull(
        stringResource(solicitud.categoria.etiqueta()),
        momentoSolicitud(solicitud, ahora),
        if (platos == 0) stringResource(R.string.solicitud_sin_carta)
        else pluralStringResource(R.plurals.carta_platos, platos, platos),
    ).joinToString(" · ")
}

@Composable
private fun momentoSolicitud(solicitud: SolicitudLocal, ahora: Long): String? {
    val revisadoEn = solicitud.revisadoEn
    return when (solicitud.estado) {
        EstadoRestaurante.APROBADO -> revisadoEn?.let { stringResource(R.string.solicitud_aprobado_el, fechaCorta(it)) }
        EstadoRestaurante.RECHAZADO -> revisadoEn?.let { stringResource(R.string.solicitud_rechazado_el, fechaCorta(it)) }
        else -> cuandoSeEnvio(solicitud, ahora)
    }
}

@Composable
private fun cuandoSeEnvio(solicitud: SolicitudLocal, ahora: Long): String? {
    if (solicitud.enviadoEn <= 0L) return null
    val minutos = ((ahora - solicitud.enviadoEn).coerceAtLeast(0L) / MILISEGUNDOS_POR_MINUTO).toInt()
    val dias = minutos / MINUTOS_POR_DIA
    val hace = when {
        minutos < 1 -> stringResource(R.string.solicitud_hace_un_momento)
        minutos < MINUTOS_POR_HORA -> stringResource(R.string.solicitud_hace_minutos, minutos)
        minutos < MINUTOS_POR_DIA -> stringResource(R.string.solicitud_hace_horas, minutos / MINUTOS_POR_HORA)
        else -> pluralStringResource(R.plurals.solicitud_hace_dias, dias, dias)
    }
    return if (solicitud.reenviado) stringResource(R.string.solicitud_reenviado_hace, hace) else hace
}

@Composable
private fun motivoAnterior(rechazo: Rechazo): String =
    textoRechazo(rechazo).replaceFirstChar { it.lowercase(Locale.forLanguageTag("es-PE")) }

@StringRes
private fun FiltroSolicitudes.etiqueta(): Int = when (this) {
    FiltroSolicitudes.PENDIENTES -> R.string.filtro_pendientes
    FiltroSolicitudes.APROBADAS -> R.string.filtro_aprobados
    FiltroSolicitudes.RECHAZADAS -> R.string.filtro_rechazados
}

@StringRes
private fun FiltroSolicitudes.tituloVacio(): Int = when (this) {
    FiltroSolicitudes.PENDIENTES -> R.string.solicitudes_vacio_titulo
    FiltroSolicitudes.APROBADAS -> R.string.solicitudes_vacio_aprobadas_titulo
    FiltroSolicitudes.RECHAZADAS -> R.string.solicitudes_vacio_rechazadas_titulo
}

@StringRes
private fun FiltroSolicitudes.textoVacio(): Int = when (this) {
    FiltroSolicitudes.PENDIENTES -> R.string.solicitudes_vacio_texto
    FiltroSolicitudes.APROBADAS -> R.string.solicitudes_vacio_aprobadas_texto
    FiltroSolicitudes.RECHAZADAS -> R.string.solicitudes_vacio_rechazadas_texto
}

private const val MILISEGUNDOS_POR_MINUTO = 60_000L
private const val MINUTOS_POR_HORA = 60
private const val MINUTOS_POR_DIA = 60 * 24

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SolicitudesPreview() {
    val ahora = System.currentTimeMillis()
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SolicitudesContenido(
                uiState = SolicitudesUiState(
                    pendientes = ListaSolicitudes(
                        cargando = false,
                        solicitudes = listOf(
                            SolicitudLocal(
                                uid = "1",
                                nombre = "La Sazón de Doña Carmen",
                                categoria = CategoriaRestaurante.CRIOLLA,
                                logoUrl = null,
                                estado = EstadoRestaurante.PENDIENTE,
                                enviadoEn = ahora - 20 * MILISEGUNDOS_POR_MINUTO,
                                revisadoEn = ahora - MINUTOS_POR_DIA * MILISEGUNDOS_POR_MINUTO,
                                reenviado = true,
                                rechazoAnterior = Rechazo(
                                    motivos = setOf(MotivoRechazo.DATOS_INCOMPLETOS, MotivoRechazo.DIRECCION_NO_VERIFICABLE),
                                    detalle = null,
                                ),
                                cantidadPlatos = 5,
                            ),
                            SolicitudLocal(
                                uid = "2",
                                nombre = "Pollería El Buen Sabor",
                                categoria = CategoriaRestaurante.POLLERIA,
                                logoUrl = null,
                                estado = EstadoRestaurante.PENDIENTE,
                                enviadoEn = ahora - 2 * MINUTOS_POR_HORA * MILISEGUNDOS_POR_MINUTO,
                                revisadoEn = null,
                                reenviado = false,
                                rechazoAnterior = null,
                                cantidadPlatos = 12,
                            ),
                            SolicitudLocal(
                                uid = "3",
                                nombre = "Chifa Dragón Dorado",
                                categoria = CategoriaRestaurante.CHIFA,
                                logoUrl = null,
                                estado = EstadoRestaurante.PENDIENTE,
                                enviadoEn = ahora - MINUTOS_POR_DIA * MILISEGUNDOS_POR_MINUTO,
                                revisadoEn = null,
                                reenviado = false,
                                rechazoAnterior = null,
                                cantidadPlatos = 0,
                            ),
                        ),
                    ),
                ),
                onSalir = {},
                onElegirFiltro = {},
                onAbrirSolicitud = {},
                onReintentar = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
