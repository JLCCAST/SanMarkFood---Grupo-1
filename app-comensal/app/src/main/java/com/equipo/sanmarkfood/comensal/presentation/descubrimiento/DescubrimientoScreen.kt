package com.equipo.sanmarkfood.comensal.presentation.descubrimiento

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import coil3.compose.AsyncImage
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.CategoriaRestaurante
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.ErrorDescubrimiento
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.FiltroDescubrimiento
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import java.util.Locale

// Tramos de precio del menú, en céntimos. Un valor null significa «sin límite».
private enum class TramoPrecio(
    @param:StringRes val etiqueta: Int,
    val minimo: Int?,
    val maximo: Int?
) {
    HASTA_10(R.string.descubrimiento_precio_hasta_10, null, 1000),
    DE_10_A_15(R.string.descubrimiento_precio_10_15, 1000, 1500),
    DE_15_A_25(R.string.descubrimiento_precio_15_25, 1500, 2500),
    MAS_DE_25(R.string.descubrimiento_precio_mas_25, 2500, null)
}

private enum class CalificacionMinima(
    @param:StringRes val etiqueta: Int,
    val valor: Double
) {
    TRES(R.string.descubrimiento_calif_3, 3.0),
    CUATRO(R.string.descubrimiento_calif_4, 4.0),
    CUATRO_Y_MEDIO(R.string.descubrimiento_calif_45, 4.5)
}

// Alto de la hoja con la lista cuando está recogida: deja ver la primera tarjeta.
private val ALTO_HOJA_RECOGIDA = 240.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DescubrimientoScreen(
    modifier: Modifier = Modifier,
    viewModel: DescubrimientoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val contexto = LocalContext.current

    // true cuando el usuario ya hizo lo que le toca: dar el permiso y encender la
    // ubicación del celular. Mientras sea false se muestra el aviso con el botón.
    var ubicacionLista by remember { mutableStateOf(ubicacionDisponible(contexto)) }

    // Vuelve a leer el estado real; si la ubicación acaba de quedar lista, recarga la lista.
    val actualizar: () -> Unit = {
        val ahora = ubicacionDisponible(contexto)
        if (ahora != ubicacionLista) {
            ubicacionLista = ahora
            if (ahora) viewModel.cargar(usarUbicacion = true)
        }
    }

    // Se actualiza al instante cuando se enciende o apaga la ubicación del celular,
    // también desde la barra de ajustes rápidos.
    DisposableEffect(contexto) {
        val receptor = object : BroadcastReceiver() {
            override fun onReceive(contexto: Context?, intent: Intent?) = actualizar()
        }
        val filtro = IntentFilter().apply {
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            addAction(LocationManager.MODE_CHANGED_ACTION)
        }
        ContextCompat.registerReceiver(
            contexto, receptor, filtro, ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose { contexto.unregisterReceiver(receptor) }
    }

    // También al volver a la app (por ejemplo, desde los ajustes del sistema).
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { actualizar() }

    // Plan B: si el cuadro de Google no está disponible, se abren los ajustes de ubicación.
    val abrirAjustesUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { actualizar() }

    // Respuesta del cuadro «activa la ubicación» de Google.
    val resolverAjustes = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { resultado ->
        actualizar()
        if (resultado.resultCode == Activity.RESULT_OK) {
            viewModel.cargar(usarUbicacion = true)
        }
    }

    // Pide encender la ubicación con el cuadro de Google, como Maps o Uber.
    val pedirActivarUbicacion: () -> Unit = {
        val peticion = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000L).build()
        val ajustes = LocationSettingsRequest.Builder().addLocationRequest(peticion).build()
        LocationServices.getSettingsClient(contexto)
            .checkLocationSettings(ajustes)
            .addOnSuccessListener {
                actualizar()
                viewModel.cargar(usarUbicacion = true)
            }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    resolverAjustes.launch(IntentSenderRequest.Builder(e.resolution).build())
                } else {
                    abrirAjustesUbicacion.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            }
    }

    val solicitarPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        val concedido = resultado.values.any { it }
        when {
            // Permiso dado pero ubicación apagada: se pide encenderla.
            concedido && !ubicacionDelCelularActiva(contexto) -> pedirActivarUbicacion()
            else -> {
                actualizar()
                viewModel.cargar(usarUbicacion = concedido)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.iniciar(usarUbicacion = tienePermisoDeUbicacion(contexto))
    }

    // Botón «Activar ubicación» del aviso de la lista.
    val activarUbicacion: () -> Unit = {
        when {
            // Sin permiso: se pide.
            !tienePermisoDeUbicacion(contexto) -> solicitarPermiso.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )

            // Con permiso, pero la ubicación del celular está apagada.
            !ubicacionDelCelularActiva(contexto) -> pedirActivarUbicacion()

            // Todo listo: se actualiza el aviso y se vuelve a intentar.
            else -> {
                actualizar()
                viewModel.cargar(usarUbicacion = true)
            }
        }
    }

    // Local elegido, ya sea tocando su pin en el mapa o su tarjeta en la lista.
    var seleccionadoId by rememberSaveable { mutableStateOf<String?>(null) }
    var panelAbierto by rememberSaveable { mutableStateOf(false) }
    val estadoLista = rememberLazyListState()
    val estadoHoja = rememberBottomSheetScaffoldState()
    val desplazamientoBotones = rememberScrollState()

    // Los botones de filtros solo tienen sentido cuando ya hay una lista que mostrar.
    val cargandoSinDatos = state.isLoading && state.restaurantes.isEmpty()
    val errorSinDatos = state.error != null && state.restaurantes.isEmpty()

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.home_tab_explorar),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )

        if (!cargandoSinDatos && !errorSinDatos) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(desplazamientoBotones)
                    .padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChipFiltro(
                    texto = stringResource(R.string.descubrimiento_boton_filtros),
                    seleccionado = state.filtro.hayFiltrosDelPanel(),
                    onClick = { panelAbierto = true }
                )
                ChipFiltro(
                    texto = stringResource(R.string.descubrimiento_chip_menu_hoy),
                    seleccionado = state.filtro.soloConMenuHoy,
                    onClick = {
                        viewModel.filtrar(
                            state.filtro.copy(soloConMenuHoy = !state.filtro.soloConMenuHoy)
                        )
                    }
                )
                ChipFiltro(
                    texto = stringResource(R.string.descubrimiento_chip_abierto_ahora),
                    seleccionado = state.filtro.soloAbiertoAhora,
                    onClick = {
                        viewModel.filtrar(
                            state.filtro.copy(soloAbiertoAhora = !state.filtro.soloAbiertoAhora)
                        )
                    }
                )
            }
        }

        when {
            cargandoSinDatos -> Cargando()

            errorSinDatos -> ErrorConReintento(
                error = state.error!!,
                onReintentar = { viewModel.cargar(tienePermisoDeUbicacion(contexto)) }
            )

            // Mapa al fondo y, encima, la lista como hoja que se desliza hacia arriba.
            else -> BottomSheetScaffold(
                scaffoldState = estadoHoja,
                modifier = Modifier.weight(1f),
                sheetPeekHeight = ALTO_HOJA_RECOGIDA,
                sheetContent = {
                    ListaRestaurantes(
                        restaurantes = state.restaurantes,
                        estadoLista = estadoLista,
                        seleccionadoId = seleccionadoId,
                        onSeleccionar = { seleccionadoId = it },
                        hayFiltros = state.filtro != FiltroDescubrimiento(),
                        onLimpiarFiltros = { viewModel.filtrar(FiltroDescubrimiento()) },
                        mostrarAviso = !ubicacionLista,
                        onActivarUbicacion = activarUbicacion
                    )
                }
            ) { relleno ->
                MapaRestaurantes(
                    restaurantes = state.restaurantes,
                    seleccionadoId = seleccionadoId,
                    onSeleccionar = { seleccionadoId = it },
                    mostrarMiUbicacion = ubicacionLista,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(relleno)
                )
            }
        }
    }

    if (panelAbierto) {
        PanelFiltros(
            filtro = state.filtro,
            onCambiar = viewModel::filtrar,
            onCerrar = { panelAbierto = false }
        )
    }
}

/** true si hay algún filtro del panel (categoría, precio o calificación); los botones no cuentan. */
private fun FiltroDescubrimiento.hayFiltrosDelPanel(): Boolean =
    categoria != null ||
            precioMinimo != null ||
            precioMaximo != null ||
            calificacionMinima != null

/** Botón en forma de chip: se ve oscuro cuando su filtro está activo. */
@Composable
private fun ChipFiltro(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(texto) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colors.onSurface,
            selectedLabelColor = colors.surface
        )
    )
}

/**
 * Panel «Filtrar locales»: Categoría, Rango de precio y Calificación. Los cambios se aplican
 * al instante; «Limpiar» borra solo estos tres filtros y «Listo» cierra el panel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PanelFiltros(
    filtro: FiltroDescubrimiento,
    onCambiar: (FiltroDescubrimiento) -> Unit,
    onCerrar: () -> Unit
) {
    val estadoPanel = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Tramo de precio que corresponde al filtro actual (ninguno si no hay filtro de precio).
    val tramoActual = TramoPrecio.entries.firstOrNull {
        it.minimo == filtro.precioMinimo && it.maximo == filtro.precioMaximo
    }
    val sinPrecio = filtro.precioMinimo == null && filtro.precioMaximo == null

    ModalBottomSheet(onDismissRequest = onCerrar, sheetState = estadoPanel) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.descubrimiento_panel_titulo),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            GrupoFiltro(titulo = stringResource(R.string.descubrimiento_filtro_categoria)) {
                ChipFiltro(
                    texto = stringResource(R.string.descubrimiento_panel_todas),
                    seleccionado = filtro.categoria == null,
                    onClick = { onCambiar(filtro.copy(categoria = null)) }
                )
                CategoriaRestaurante.entries.forEach { categoria ->
                    ChipFiltro(
                        texto = stringResource(categoria.etiqueta()),
                        seleccionado = filtro.categoria == categoria,
                        onClick = { onCambiar(filtro.copy(categoria = categoria)) }
                    )
                }
            }

            GrupoFiltro(titulo = stringResource(R.string.descubrimiento_filtro_rango_precio)) {
                ChipFiltro(
                    texto = stringResource(R.string.descubrimiento_panel_todos),
                    seleccionado = sinPrecio,
                    onClick = { onCambiar(filtro.copy(precioMinimo = null, precioMaximo = null)) }
                )
                TramoPrecio.entries.forEach { tramo ->
                    ChipFiltro(
                        texto = stringResource(tramo.etiqueta),
                        seleccionado = tramoActual == tramo,
                        onClick = {
                            onCambiar(
                                filtro.copy(precioMinimo = tramo.minimo, precioMaximo = tramo.maximo)
                            )
                        }
                    )
                }
            }

            GrupoFiltro(titulo = stringResource(R.string.descubrimiento_filtro_calificacion)) {
                ChipFiltro(
                    texto = stringResource(R.string.descubrimiento_panel_todas),
                    seleccionado = filtro.calificacionMinima == null,
                    onClick = { onCambiar(filtro.copy(calificacionMinima = null)) }
                )
                CalificacionMinima.entries.forEach { calificacion ->
                    ChipFiltro(
                        texto = stringResource(calificacion.etiqueta),
                        seleccionado = filtro.calificacionMinima == calificacion.valor,
                        onClick = {
                            onCambiar(filtro.copy(calificacionMinima = calificacion.valor))
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        onCambiar(
                            filtro.copy(
                                categoria = null,
                                precioMinimo = null,
                                precioMaximo = null,
                                calificacionMinima = null
                            )
                        )
                    },
                    enabled = filtro.hayFiltrosDelPanel()
                ) {
                    Text(stringResource(R.string.descubrimiento_limpiar_filtros))
                }
                Button(onClick = onCerrar, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.descubrimiento_panel_listo))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GrupoFiltro(titulo: String, opciones: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            opciones()
        }
    }
}

@Composable
private fun ListaRestaurantes(
    restaurantes: List<Restaurante>,
    estadoLista: LazyListState,
    seleccionadoId: String?,
    onSeleccionar: (String) -> Unit,
    hayFiltros: Boolean,
    onLimpiarFiltros: () -> Unit,
    mostrarAviso: Boolean,
    onActivarUbicacion: () -> Unit
) {
    // Al elegir un local (por ejemplo, tocando su pin) la lista se desplaza hasta su tarjeta.
    // El aviso de ubicación, si está, ocupa el primer lugar de la lista.
    val lugaresAntes = if (mostrarAviso) 1 else 0
    LaunchedEffect(seleccionadoId) {
        val indice = restaurantes.indexOfFirst { it.id == seleccionadoId }
        if (indice >= 0) estadoLista.animateScrollToItem(indice + lugaresAntes)
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = estadoLista,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (mostrarAviso) {
            item { AvisoUbicacion(onActivar = onActivarUbicacion) }
        }

        if (restaurantes.isEmpty()) {
            item {
                if (hayFiltros) {
                    SinCoincidencias(onLimpiar = onLimpiarFiltros)
                } else {
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
        }

        items(restaurantes, key = { it.id }) { restaurante ->
            TarjetaRestaurante(
                restaurante = restaurante,
                seleccionado = restaurante.id == seleccionadoId,
                onClick = { onSeleccionar(restaurante.id) }
            )
        }
    }
}

@Composable
private fun SinCoincidencias(onLimpiar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.descubrimiento_sin_coincidencias),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        OutlinedButton(onClick = onLimpiar) {
            Text(stringResource(R.string.descubrimiento_limpiar_filtros))
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
private fun TarjetaRestaurante(
    restaurante: Restaurante,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val forma = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .clip(forma)
            .clickable(onClick = onClick),
        shape = forma,
        color = if (seleccionado) colors.primaryContainer else colors.surfaceContainerHigh
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

/** true si la app tiene el permiso y la ubicación del celular está encendida. */
private fun ubicacionDisponible(contexto: Context): Boolean =
    tienePermisoDeUbicacion(contexto) && ubicacionDelCelularActiva(contexto)

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