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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.horaCierraHoy
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.menuVigente
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import java.util.Locale

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

private val ALTO_HOJA_RECOGIDA = 240.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DescubrimientoScreen(
    modifier: Modifier = Modifier,
    restauranteAEnfocar: String? = null,
    onRestauranteClick: (String) -> Unit = {},
    viewModel: DescubrimientoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val contexto = LocalContext.current

    var ubicacionLista by remember { mutableStateOf(ubicacionDisponible(contexto)) }

    val actualizar: () -> Unit = {
        val ahora = ubicacionDisponible(contexto)
        if (ahora != ubicacionLista) {
            ubicacionLista = ahora
            if (ahora) viewModel.cargar(usarUbicacion = true)
        }
    }

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

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { actualizar() }

    val abrirAjustesUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { actualizar() }

    val resolverAjustes = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { resultado ->
        actualizar()
        if (resultado.resultCode == Activity.RESULT_OK) {
            viewModel.cargar(usarUbicacion = true)
        }
    }

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

    val activarUbicacion: () -> Unit = {
        when {
            !tienePermisoDeUbicacion(contexto) -> solicitarPermiso.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            !ubicacionDelCelularActiva(contexto) -> pedirActivarUbicacion()
            else -> {
                actualizar()
                viewModel.cargar(usarUbicacion = true)
            }
        }
    }

    var seleccionadoId by rememberSaveable { mutableStateOf<String?>(null) }
    var panelAbierto by rememberSaveable { mutableStateOf(false) }

    // Primer toque (tarjeta o pin) selecciona y centra; tocar de nuevo lo ya seleccionado abre el detalle.
    val onRestauranteSeleccionado: (String) -> Unit = { id ->
        if (seleccionadoId == id) {
            onRestauranteClick(id)
        } else {
            seleccionadoId = id
        }
    }

    // Viene de "Ver en el mapa" en el detalle: selecciona y centra sin pasar por onRestauranteSeleccionado.
    LaunchedEffect(restauranteAEnfocar) {
        if (restauranteAEnfocar != null) seleccionadoId = restauranteAEnfocar
    }
    val estadoLista = rememberLazyListState()
    val estadoHoja = rememberBottomSheetScaffoldState()
    val desplazamientoBotones = rememberScrollState()

    val cargandoSinDatos = state.isLoading && state.restaurantes.isEmpty()
    val errorSinDatos = state.error != null && state.restaurantes.isEmpty()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            cargandoSinDatos -> Cargando()

            errorSinDatos -> ErrorConReintento(
                error = state.error!!,
                onReintentar = { viewModel.cargar(tienePermisoDeUbicacion(contexto)) }
            )

            else -> {
                BottomSheetScaffold(
                    scaffoldState = estadoHoja,
                    modifier = Modifier.fillMaxSize(),
                    sheetPeekHeight = ALTO_HOJA_RECOGIDA,
                    sheetContent = {
                        ListaRestaurantes(
                            restaurantes = state.restaurantes,
                            estadoLista = estadoLista,
                            seleccionadoId = seleccionadoId,
                            onSeleccionar = onRestauranteSeleccionado,
                            hayFiltros = state.filtro != FiltroDescubrimiento(),
                            onLimpiarFiltros = { viewModel.filtrar(FiltroDescubrimiento()) },
                            mostrarAviso = !ubicacionLista,
                            ubicacionActiva = ubicacionLista,
                            onActivarUbicacion = activarUbicacion
                        )
                    }
                ) { relleno ->
                    MapaRestaurantes(
                        restaurantes = state.restaurantes,
                        seleccionadoId = seleccionadoId,
                        onSeleccionar = onRestauranteSeleccionado,
                        mostrarMiUbicacion = ubicacionLista,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(relleno)
                    )
                }

                // Chips flotando sobre el mapa
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(desplazamientoBotones)
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
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
                    ChipFiltro(
                        texto = stringResource(R.string.descubrimiento_chip_favoritos),
                        seleccionado = state.filtro.soloFavoritos,
                        onClick = {
                            viewModel.filtrar(
                                state.filtro.copy(soloFavoritos = !state.filtro.soloFavoritos)
                            )
                        }
                    )
                }
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

private fun FiltroDescubrimiento.hayFiltrosDelPanel(): Boolean =
    categoria != null ||
            precioMinimo != null ||
            precioMaximo != null ||
            calificacionMinima != null

@Composable
private fun ChipFiltro(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        border = null
    ) {
        FilterChip(
            selected = seleccionado,
            onClick = onClick,
            label = { Text(texto) },
            border = null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.onSurface,
                selectedLabelColor = MaterialTheme.colorScheme.surface,
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PanelFiltros(
    filtro: FiltroDescubrimiento,
    onCambiar: (FiltroDescubrimiento) -> Unit,
    onCerrar: () -> Unit
) {
    val estadoPanel = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
    ubicacionActiva: Boolean,
    onActivarUbicacion: () -> Unit
) {
    val elementosEncabezado = if (mostrarAviso) 2 else 1

    LaunchedEffect(seleccionadoId) {
        val indice = restaurantes.indexOfFirst { it.id == seleccionadoId }
        if (indice >= 0) estadoLista.animateScrollToItem(indice + elementosEncabezado)
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

        item {
            CabeceraLista(
                cantidad = restaurantes.size,
                ubicacionActiva = ubicacionActiva
            )
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
private fun CabeceraLista(cantidad: Int, ubicacionActiva: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.descubrimiento_cerca_de_ti),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )

        val textoConteo = if (ubicacionActiva) {
            stringResource(R.string.descubrimiento_conteo_cercania, cantidad)
        } else {
            stringResource(R.string.descubrimiento_conteo_zona, cantidad)
        }

        Text(
            text = textoConteo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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

    val fondo = if (seleccionado) Color(0xFFFFF8F6) else colors.surfaceContainerHigh
    val borde = if (seleccionado) BorderStroke(1.5.dp, Color(0xFF8B1D24)) else null

    Surface(
        modifier = Modifier
            .clip(forma)
            .clickable(onClick = onClick),
        shape = forma,
        color = fondo,
        border = borde
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

                val partesDetalle = mutableListOf<String>()
                partesDetalle.add(stringResource(restaurante.categoria.etiqueta()))

                val promedio = restaurante.calificacionPromedio
                if (promedio != null && restaurante.totalResenas > 0) {
                    partesDetalle.add("★ ${formatearDecimal(promedio)}")
                }

                restaurante.distanciaMetros?.let { metros ->
                    partesDetalle.add(textoDistancia(metros))
                }

                Text(
                    text = partesDetalle.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )

                val horaCierra = restaurante.horaCierraHoy()
                if (horaCierra != null) {
                    Text(
                        text = "Abierto · hasta las $horaCierra",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.descubrimiento_cerrado),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.error
                    )
                }
            }

            val menu = restaurante.menuVigente()
            if (menu != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "MENÚ HOY",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurfaceVariant
                    )
                    Text(
                        text = "S/ ${menu.precio / 100}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF8B1D24)
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
        "${metros} m"
    } else {
        "${formatearDecimal(metros / 1000.0)} km"
    }

private fun formatearDecimal(valor: Double): String =
    String.format(Locale.forLanguageTag("es-PE"), "%.1f", valor)

private fun tienePermisoDeUbicacion(contexto: Context): Boolean =
    ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

private fun ubicacionDelCelularActiva(contexto: Context): Boolean {
    val administrador = contexto.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    return administrador != null && LocationManagerCompat.isLocationEnabled(administrador)
}

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