package com.equipo.sanmarkfood.comensal.presentation.descubrimiento

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
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
import kotlin.math.roundToLong

// Centro de Lima: posición inicial mientras no hay locales con ubicación.
private val CENTRO_LIMA = LatLng(-12.0464, -77.0428)

// Zoom cuando todos los puntos están casi en el mismo lugar: se ven las calles cercanas.
private const val ZOOM_CERCA = 16f

// Si todos los puntos caben en menos de este rango (en grados, unos 50 m), no se encuadra: se acerca.
private const val RANGO_MINIMO_GRADOS = 0.0005

// Margen en píxeles entre los pines y el borde del mapa.
private const val MARGEN_ENCUADRE = 150

// Cuánto sube el nombre de cada local que comparte lugar con otro, por nivel.
private val ALTO_NIVEL_NOMBRE = 30.dp

/**
 * Mapa con un pin por local: el nombre del restaurante sobre un icono de ubicación.
 * Dibuja la misma lista que la pantalla, así que respeta los filtros.
 * El encuadre incluye al usuario (si hay ubicación) y todos los locales, con margen.
 * Los botones + y − del mapa quedan a un costado, como en Google Maps.
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

    // Altura del nombre de cada local: 0 si está solo; 1, 2... si comparte lugar con otros.
    val niveles = remember(restaurantes) { nivelesPorLugar(restaurantes) }

    // Lee la posición del usuario cuando la ubicación queda lista; la borra si se apaga.
    LaunchedEffect(mostrarMiUbicacion) {
        miPosicion = if (mostrarMiUbicacion) obtenerPosicionActual(contexto) else null
    }

    // Encuadra al usuario y a los locales cada vez que cambia la lista (por ejemplo, al filtrar)
    // o llega la posición del usuario.
    LaunchedEffect(mapaCargado, puntos, miPosicion) {
        if (!mapaCargado) return@LaunchedEffect
        val todos = puntos + listOfNotNull(miPosicion)
        if (todos.isEmpty()) return@LaunchedEffect
        camara.animate(encuadrar(todos))
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
            val cerrado = restaurante.etiquetaPin() == EtiquetaPin.Cerrado
            val nivel = niveles[restaurante.id] ?: 0

            key(restaurante.id) {
                MarkerComposable(
                    restaurante.nombre,
                    cerrado,
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
                        nombre = restaurante.nombre,
                        cerrado = cerrado,
                        seleccionado = seleccionado,
                        nivel = nivel
                    )
                }
            }
        }
    }
}

/**
 * Los locales que están en el mismo lugar (a menos de unos 11 m) se reparten niveles 0, 1, 2...
 * para que sus nombres no se tapen. Un local solo siempre queda en el nivel 0.
 */
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

/** Mueve la cámara para que entren todos los puntos con margen; se acerca si están juntos. */
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

/** Posición actual del celular, o null si no se pudo leer. Solo se llama con el permiso dado. */
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
private fun PinRestaurante(nombre: String, cerrado: Boolean, seleccionado: Boolean, nivel: Int) {
    val colors = MaterialTheme.colorScheme

    val fondo = when {
        seleccionado -> colors.primary
        cerrado -> colors.surfaceVariant
        else -> colors.surface
    }
    val letra = when {
        seleccionado -> colors.onPrimary
        cerrado -> colors.onSurfaceVariant
        else -> colors.onSurface
    }
    val colorIcono = if (cerrado) colors.onSurfaceVariant else colors.primary

    // El icono queda abajo y al centro: su punta es el lugar exacto del local en el mapa.
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = RoundedCornerShape(50), color = fondo, shadowElevation = 4.dp) {
            Text(
                text = nombre,
                modifier = Modifier
                    .widthIn(max = 160.dp)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = letra,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        // Línea que une el nombre con el icono cuando el nombre sube por compartir lugar.
        if (nivel > 0) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(ALTO_NIVEL_NOMBRE * nivel)
                    .background(colorIcono)
            )
        }
        Icon(
            imageVector = Icons.Filled.Place,
            contentDescription = null,
            modifier = Modifier.size(if (seleccionado) 36.dp else 30.dp),
            tint = colorIcono
        )
    }
}