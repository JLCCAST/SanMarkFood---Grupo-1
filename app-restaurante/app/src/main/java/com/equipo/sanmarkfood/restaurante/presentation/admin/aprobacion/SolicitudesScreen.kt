package com.equipo.sanmarkfood.restaurante.presentation.admin.aprobacion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.admin.SolicitudLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.MotivoRechazo
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Rechazo
import com.equipo.sanmarkfood.restaurante.presentation.admin.CabeceraAdministracion
import com.equipo.sanmarkfood.restaurante.presentation.admin.MensajeErrorAdmin
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.LogoLocal
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.etiqueta
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.titulo
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme
import java.util.Locale

@Composable
fun SolicitudesScreen(
    onSalir: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SolicitudesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SolicitudesContenido(
        uiState = uiState,
        onSalir = onSalir,
        onReintentar = viewModel::onReintentar,
        modifier = modifier,
    )
}

@Composable
private fun SolicitudesContenido(
    uiState: SolicitudesUiState,
    onSalir: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        CabeceraAdministracion(
            titulo = stringResource(R.string.solicitudes_titulo),
            subtitulo = stringResource(R.string.solicitudes_subtitulo),
            onSalir = onSalir,
        )

        val error = uiState.error
        when {
            uiState.cargando -> Box(
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

            uiState.solicitudes.isEmpty() -> Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.solicitudes_vacio_titulo),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.solicitudes_vacio_texto),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            else -> {
                val ahora = remember(uiState.solicitudes) { System.currentTimeMillis() }
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.solicitudes, key = { it.uid }) { solicitud ->
                        TarjetaSolicitud(solicitud = solicitud, ahora = ahora)
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaSolicitud(solicitud: SolicitudLocal, ahora: Long) {
    val forma = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) {},
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
                EtiquetaSolicitud(reenviado = solicitud.reenviado)
            }
            Text(
                text = detalleSolicitud(solicitud, ahora),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            solicitud.rechazoAnterior?.let { rechazo ->
                Text(
                    text = stringResource(R.string.solicitud_rechazo_anterior, motivoAnterior(rechazo)),
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
    }
}

@Composable
private fun EtiquetaSolicitud(reenviado: Boolean) {
    val fondo = if (reenviado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val texto = if (reenviado) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    Text(
        text = stringResource(if (reenviado) R.string.solicitud_reenviado else R.string.solicitud_nuevo),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(fondo)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall,
        color = texto,
    )
}

@Composable
private fun detalleSolicitud(solicitud: SolicitudLocal, ahora: Long): String {
    val platos = solicitud.cantidadPlatos
    return listOfNotNull(
        stringResource(solicitud.categoria.etiqueta()),
        cuandoSeEnvio(solicitud, ahora),
        if (platos == 0) stringResource(R.string.solicitud_sin_carta)
        else pluralStringResource(R.plurals.carta_platos, platos, platos),
    ).joinToString(" · ")
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
private fun motivoAnterior(rechazo: Rechazo): String {
    val detalle = rechazo.detalle
    if (rechazo.motivo == MotivoRechazo.OTRO && detalle != null) return detalle
    return stringResource(rechazo.motivo.titulo()).replaceFirstChar { it.lowercase(Locale.forLanguageTag("es-PE")) }
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
                    cargando = false,
                    solicitudes = listOf(
                        SolicitudLocal(
                            uid = "1",
                            nombre = "La Sazón de Doña Carmen",
                            categoria = CategoriaRestaurante.CRIOLLA,
                            logoUrl = null,
                            enviadoEn = ahora - 20 * MILISEGUNDOS_POR_MINUTO,
                            reenviado = true,
                            rechazoAnterior = Rechazo(MotivoRechazo.DIRECCION_NO_VERIFICABLE, detalle = null),
                            cantidadPlatos = 5,
                        ),
                        SolicitudLocal(
                            uid = "2",
                            nombre = "Pollería El Buen Sabor",
                            categoria = CategoriaRestaurante.POLLERIA,
                            logoUrl = null,
                            enviadoEn = ahora - 2 * MINUTOS_POR_HORA * MILISEGUNDOS_POR_MINUTO,
                            reenviado = false,
                            rechazoAnterior = null,
                            cantidadPlatos = 12,
                        ),
                        SolicitudLocal(
                            uid = "3",
                            nombre = "Chifa Dragón Dorado",
                            categoria = CategoriaRestaurante.CHIFA,
                            logoUrl = null,
                            enviadoEn = ahora - MINUTOS_POR_DIA * MILISEGUNDOS_POR_MINUTO,
                            reenviado = false,
                            rechazoAnterior = null,
                            cantidadPlatos = 0,
                        ),
                    ),
                ),
                onSalir = {},
                onReintentar = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
