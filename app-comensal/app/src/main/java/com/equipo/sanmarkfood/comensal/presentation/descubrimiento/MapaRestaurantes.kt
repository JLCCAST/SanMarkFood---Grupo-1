package com.equipo.sanmarkfood.comensal.presentation.descubrimiento

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.EtiquetaPin
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.etiquetaPin
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.math.roundToLong

private val CENTRO_LIMA = LatLng(-12.0464, -77.0428)
private const val ZOOM_CERCA = 16f
private const val RANGO_MINIMO_GRADOS = 0.0005
private const val MARGEN_ENCUADRE = 150
private val ALTO_NIVEL_ETIQUETA = 36.dp

private val FormaPunta = GenericShape { tamano, _ ->
    moveTo(0f, 0f)
    lineTo(tamano.width, 0f)
    lineTo(tamano.width / 2f, tamano.height)
    close()
}

/**
 * Mapa con un pin por local (SCRUM-149).
 * Muestra el encuadre con los locales e incluye al usuario si la ubicación está activa.
 */
@Composable
fun MapaRestaurantes(
    restaurantes: List<Restaurante>,
    seleccionadoId: String?,
    onSeleccionar: (String) -> Unit,
    mostrarMiUbicacion: Boolean,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val camara = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(CENTRO_LIMA, 12f)
    }
    var mapaCargado by remember { mutableStateOf(false) }
    var miPosicion by remember { mutableStateOf<LatLng?>(null) }

    val puntos = restaurantes.mapNotNull { restaurante ->
        restaurante.ubicacion?.let { LatLng(it.latitud, it.longitud) }
    }

    val niveles = remember(restaurantes) { nivelesPorLugar(restaurantes) }

    LaunchedEffect(mostrarMiUbicacion) {
        miPosicion = if (mostrarMiUbicacion) obtenerPosicionActual(contexto) else null
    }

    // Encuadre inicial o cuando cambia la lista de restaurantes o mi posición.
    LaunchedEffect(mapaCargado, puntos, miPosicion) {
        if (!mapaCargado) return@LaunchedEffect
        val todos = puntos + listOfNotNull(miPosicion)
        if (todos.isEmpty()) return@LaunchedEffect
        camara.animate(encuadrar(todos))
    }

    // Centra el mapa en el local elegido al tocar su tarjeta en la lista.
    LaunchedEffect(seleccionadoId) {
        if (!mapaCargado || seleccionadoId == null) return@LaunchedEffect
        val objetivo = restaurantes.firstOrNull { it.id == seleccionadoId }?.ubicacion ?: return@LaunchedEffect
        camara.animate(CameraUpdateFactory.newLatLngZoom(LatLng(objetivo.latitud, objetivo.longitud), ZOOM_CERCA))
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = camara,
        properties = MapProperties(isMyLocationEnabled = mostrarMiUbicacion),
        uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = false),
        contentDescription = stringResource(R.string.mapa_descripcion),
        onMapLoaded = { mapaCargado = true }
    ) {
        restaurantes.forEach { restaurante ->
            val ubicacion = restaurante.ubicacion ?: return@forEach
            val seleccionado = restaurante.id == seleccionadoId
            val etiqueta = restaurante.etiquetaPin()
            val nivel = niveles[restaurante.id] ?: 0

            key(restaurante.id) {
                MarkerComposable(
                    etiqueta,
                    seleccionado,
                    nivel,
                    state = rememberUpdatedMarkerState(
                        position = LatLng(ubicacion.latitud, ubicacion.longitud)
                    ),
                    zIndex = if (seleccionado) 1f else 0f,
                    onClick = {
                        onSeleccionar(restaurante.id)
                        true
                    }
                ) {
                    PinRestaurante(
                        etiqueta = etiqueta,
                        seleccionado = seleccionado,
                        nivel = nivel
                    )
                }
            }
        }
    }
}

private fun nivelesPorLugar(restaurantes: List<Restaurante>): Map<String, Int> {
    val niveles = mutableMapOf<String, Int>()
    restaurantes
        .filter { it.ubicacion != null }
        .groupBy { restaurante ->
            val ubicacion = restaurante.ubicacion!!
            Pair(
                (ubicacion.latitud * 10_000).roundToLong(),
                (ubicacion.longitud * 10_000).roundToLong()
            )
        }
        .values
        .forEach { grupo ->
            grupo.sortedBy { it.id }.forEachIndexed { indice, restaurante ->
                niveles[restaurante.id] = indice
            }
        }
    return niveles
}

private fun encuadrar(puntos: List<LatLng>): CameraUpdate {
    val latMin = puntos.minOf { it.latitude }
    val latMax = puntos.maxOf { it.latitude }
    val lngMin = puntos.minOf { it.longitude }
    val lngMax = puntos.maxOf { it.longitude }

    if (latMax - latMin < RANGO_MINIMO_GRADOS && lngMax - lngMin < RANGO_MINIMO_GRADOS) {
        val centro = LatLng((latMin + latMax) / 2, (lngMin + lngMax) / 2)
        return CameraUpdateFactory.newLatLngZoom(centro, ZOOM_CERCA)
    }

    val limites = LatLngBounds.builder().apply { puntos.forEach { include(it) } }.build()
    return CameraUpdateFactory.newLatLngBounds(limites, MARGEN_ENCUADRE)
}

@SuppressLint("MissingPermission")
private suspend fun obtenerPosicionActual(contexto: Context): LatLng? =
    try {
        LocationServices.getFusedLocationProviderClient(contexto)
            .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .await()
            ?.let { LatLng(it.latitude, it.longitude) }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

@Composable
private fun PinRestaurante(etiqueta: EtiquetaPin, seleccionado: Boolean, nivel: Int) {
    val colors = MaterialTheme.colorScheme
    val cerrado = etiqueta == EtiquetaPin.Cerrado

    val texto = when (etiqueta) {
        is EtiquetaPin.Precio ->
            stringResource(R.string.mapa_pin_precio, formatearSoles(etiqueta.centimos))
        is EtiquetaPin.Nombre -> etiqueta.texto
        EtiquetaPin.Cerrado -> stringResource(R.string.mapa_pin_cerrado)
    }
    val fondo = when {
        seleccionado -> colors.primary
        cerrado -> colors.onSurfaceVariant
        else -> colors.onSurface
    }
    val letra = if (seleccionado) colors.onPrimary else colors.surface

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = RoundedCornerShape(50), color = fondo, shadowElevation = 4.dp) {
            Text(
                text = texto,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = letra
            )
        }
        if (nivel > 0) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(ALTO_NIVEL_ETIQUETA * nivel)
                    .background(fondo)
            )
        }
        Box(
            modifier = Modifier
                .width(14.dp)
                .height(8.dp)
                .background(color = fondo, shape = FormaPunta)
        )
    }
}

private fun formatearSoles(centimos: Int): String =
    if (centimos % 100 == 0) {
        (centimos / 100).toString()
    } else {
        String.format(Locale.forLanguageTag("es-PE"), "%.2f", centimos / 100.0)
    }