package com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.Ubicacion
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

private const val ZOOM_VISTA_PREVIA = 16f
private const val ZOOM_SELECTOR = 17f

/** Mapa pequeño de R3: solo muestra el punto. Para moverlo se abre [SelectorUbicacion]. */
@Composable
fun VistaPreviaMapa(ubicacion: Ubicacion, onMoverPunto: () -> Unit, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(12.dp)
    val descripcion = stringResource(R.string.datos_local_mapa_descripcion)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(forma)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center,
    ) {
        if (LocalInspectionMode.current) {
            Box(modifier = Modifier.matchParentSize().background(MaterialTheme.colorScheme.surfaceContainer))
        } else {
            val camara = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(ubicacion.aLatLng(), ZOOM_VISTA_PREVIA)
            }
            LaunchedEffect(ubicacion) {
                camara.position = CameraPosition.fromLatLngZoom(ubicacion.aLatLng(), ZOOM_VISTA_PREVIA)
            }
            GoogleMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = camara,
                // Modo lite: una imagen estática, liviana dentro de un formulario con scroll.
                googleMapOptionsFactory = { GoogleMapOptions().liteMode(true) },
                uiSettings = MapUiSettings(
                    mapToolbarEnabled = false,
                    rotationGesturesEnabled = false,
                    scrollGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    zoomControlsEnabled = false,
                    zoomGesturesEnabled = false,
                ),
                // Sin este listener, en modo lite tocar el mapa abre la app de Google Maps.
                onMapClick = { onMoverPunto() },
            )
        }
        PinUbicacion(alto = 34.dp)
        Surface(
            onClick = onMoverPunto,
            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).height(36.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shadowElevation = 2.dp,
        ) {
            Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.datos_local_mover_punto), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** Mapa a pantalla completa con el punto fijo al centro: el local mueve el mapa debajo del punto. */
@Composable
fun SelectorUbicacion(
    ubicacionInicial: Ubicacion,
    onConfirmar: (Ubicacion) -> Unit,
    onCancelar: () -> Unit,
) {
    val camara = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ubicacionInicial.aLatLng(), ZOOM_SELECTOR)
    }
    BackHandler(onBack = onCancelar)

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Row(
            modifier = Modifier.height(64.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onCancelar) {
                Icon(
                    painter = painterResource(R.drawable.ic_volver),
                    contentDescription = stringResource(R.string.volver),
                )
            }
            Text(text = stringResource(R.string.ubicacion_titulo), style = MaterialTheme.typography.titleMedium)
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            GoogleMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = camara,
                uiSettings = MapUiSettings(
                    mapToolbarEnabled = false,
                    rotationGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                ),
            )
            PinUbicacion(alto = 51.dp)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.ubicacion_ayuda),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BotonPrincipal(
                texto = stringResource(R.string.ubicacion_confirmar),
                onClick = {
                    val centro = camara.position.target
                    onConfirmar(Ubicacion(latitud = centro.latitude, longitud = centro.longitude))
                },
            )
        }
    }
}

// La punta del pin queda en el centro del mapa, que es la ubicación que se guarda.
@Composable
private fun PinUbicacion(alto: Dp) {
    Icon(
        painter = painterResource(R.drawable.ic_ubicacion),
        contentDescription = null,
        modifier = Modifier
            .offset(y = -alto / 2)
            .size(width = alto * 26 / 34, height = alto),
        tint = MaterialTheme.colorScheme.primary,
    )
}

private fun Ubicacion.aLatLng() = LatLng(latitud, longitud)
