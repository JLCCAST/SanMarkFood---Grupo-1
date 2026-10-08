package com.equipo.sanmarkfood.comensal.presentation.descubrimiento

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.CategoriaRestaurante
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.ErrorDescubrimiento
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import java.util.Locale

@Composable
fun DescubrimientoScreen(
    modifier: Modifier = Modifier,
    viewModel: DescubrimientoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val contexto = LocalContext.current

    val solicitarPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        viewModel.cargar(usarUbicacion = resultado.values.any { it })
    }

    // Al volver de los ajustes de ubicación se recarga la lista.
    val abrirAjustesUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.cargar(usarUbicacion = tienePermisoDeUbicacion(contexto))
    }

    LaunchedEffect(Unit) {
        viewModel.iniciar(usarUbicacion = tienePermisoDeUbicacion(contexto))
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.home_tab_explorar),
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )

        when {
            state.isLoading && state.restaurantes.isEmpty() -> Cargando()

            state.error != null && state.restaurantes.isEmpty() -> ErrorConReintento(
                error = state.error!!,
                onReintentar = { viewModel.cargar(tienePermisoDeUbicacion(contexto)) }
            )

            else -> ListaRestaurantes(
                restaurantes = state.restaurantes,
                conDistancia = state.conDistancia,
                onActivarUbicacion = {
                    when {
                        // Sin permiso: se pide.
                        !tienePermisoDeUbicacion(contexto) -> solicitarPermiso.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )

                        // Con permiso, pero la ubicación del celular está apagada: ajustes.
                        !ubicacionDelCelularActiva(contexto) -> abrirAjustesUbicacion.launch(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        )

                        // Con permiso y ubicación encendida: se vuelve a intentar.
                        else -> viewModel.cargar(usarUbicacion = true)
                    }
                }
            )
        }
    }
}

@Composable
private fun ListaRestaurantes(
    restaurantes: List<Restaurante>,
    conDistancia: Boolean,
    onActivarUbicacion: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!conDistancia) {
            item { AvisoUbicacion(onActivar = onActivarUbicacion) }
        }

        if (restaurantes.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.descubrimiento_vacio),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        items(restaurantes, key = { it.id }) { restaurante ->
            TarjetaRestaurante(restaurante)
        }
    }
}

@Composable
private fun AvisoUbicacion(onActivar: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.secondaryContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.descubrimiento_aviso_ubicacion),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSecondaryContainer
            )
            Button(onClick = onActivar) {
                Text(stringResource(R.string.descubrimiento_boton_ubicacion))
            }
        }
    }
}

@Composable
private fun TarjetaRestaurante(restaurante: Restaurante) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Miniatura(restaurante)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = restaurante.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(restaurante.categoria.etiqueta()),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )

                val promedio = restaurante.calificacionPromedio
                Text(
                    text = if (promedio != null && restaurante.totalResenas > 0) {
                        pluralStringResource(
                            R.plurals.descubrimiento_calificacion,
                            restaurante.totalResenas,
                            formatearDecimal(promedio),
                            restaurante.totalResenas
                        )
                    } else {
                        stringResource(R.string.descubrimiento_sin_resenas)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )

                restaurante.distanciaMetros?.let { metros ->
                    Text(
                        text = textoDistancia(metros),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (restaurante.pausado) {
                    Text(
                        text = stringResource(R.string.descubrimiento_cerrado),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.error
                    )
                }
            }
        }
    }
}

@Composable
private fun Miniatura(restaurante: Restaurante) {
    val colors = MaterialTheme.colorScheme
    val url = restaurante.logoUrl.ifBlank { restaurante.portadaUrl }
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (url.isNotBlank()) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = restaurante.nombre.firstOrNull()?.uppercase().orEmpty(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun Cargando() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorConReintento(error: ErrorDescubrimiento, onReintentar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(error.mensaje()),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        OutlinedButton(onClick = onReintentar) {
            Text(stringResource(R.string.descubrimiento_reintentar))
        }
    }
}

@Composable
private fun textoDistancia(metros: Int): String =
    if (metros < 1000) {
        stringResource(R.string.descubrimiento_distancia_m, metros)
    } else {
        stringResource(R.string.descubrimiento_distancia_km, formatearDecimal(metros / 1000.0))
    }

private fun formatearDecimal(valor: Double): String =
    String.format(Locale.forLanguageTag("es-PE"), "%.1f", valor)

private fun tienePermisoDeUbicacion(contexto: Context): Boolean =
    ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

/** true si el interruptor de ubicación del celular está encendido. */
private fun ubicacionDelCelularActiva(contexto: Context): Boolean {
    val administrador = contexto.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    return administrador != null && LocationManagerCompat.isLocationEnabled(administrador)
}

@StringRes
private fun ErrorDescubrimiento.mensaje(): Int = when (this) {
    ErrorDescubrimiento.SinConexion -> R.string.error_descubrimiento_sin_conexion
    ErrorDescubrimiento.Desconocido -> R.string.error_descubrimiento_desconocido
}

@StringRes
private fun CategoriaRestaurante.etiqueta(): Int = when (this) {
    CategoriaRestaurante.CRIOLLA -> R.string.descubrimiento_categoria_criolla
    CategoriaRestaurante.CHIFA -> R.string.descubrimiento_categoria_chifa
    CategoriaRestaurante.POLLERIA -> R.string.descubrimiento_categoria_polleria
    CategoriaRestaurante.MARINA -> R.string.descubrimiento_categoria_marina
    CategoriaRestaurante.VEGETARIANA -> R.string.descubrimiento_categoria_vegetariana
    CategoriaRestaurante.OTRA -> R.string.descubrimiento_categoria_otra
}