package com.equipo.sanmarkfood.restaurante.presentation.pedidos

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.ErrorPedidos
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.EstadoPago
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.EstadoPedido
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.ItemPedido
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.MetodoPago
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.Modalidad
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.TIEMPOS_ESTIMADOS
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal
import com.equipo.sanmarkfood.restaurante.presentation.menu.precioTexto
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun BandejaPedidos(
    modifier: Modifier = Modifier,
    viewModel: PedidosViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BandejaPedidosContenido(
        uiState = uiState,
        onReintentar = viewModel::onReintentar,
        onAceptar = viewModel::onAceptar,
        onElegirMinutos = viewModel::onElegirMinutos,
        onCerrarHoja = viewModel::onCerrarHoja,
        onConfirmarAceptar = viewModel::onConfirmarAceptar,
        onAceptadoMostrado = viewModel::onAceptadoMostrado,
        modifier = modifier,
    )
}

@Composable
fun BandejaPedidosContenido(
    uiState: PedidosUiState,
    onReintentar: () -> Unit,
    onAceptar: (Pedido) -> Unit,
    onElegirMinutos: (Int) -> Unit,
    onCerrarHoja: () -> Unit,
    onConfirmarAceptar: () -> Unit,
    onAceptadoMostrado: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val avisos = remember { SnackbarHostState() }
    val aceptado = uiState.aceptado
    val textoAceptado = aceptado?.let { stringResource(R.string.pedidos_aceptado, it.comensal, horaLima(it.listoA)) }
    val ok = stringResource(R.string.pedidos_ok)
    LaunchedEffect(aceptado) {
        if (textoAceptado != null) {
            avisos.showSnackbar(textoAceptado, actionLabel = ok, duration = SnackbarDuration.Long)
            onAceptadoMostrado()
        }
    }

    Box(modifier = modifier) {
        ListaPedidos(uiState = uiState, onReintentar = onReintentar, onAceptar = onAceptar)
        SnackbarHost(hostState = avisos, modifier = Modifier.align(Alignment.BottomCenter))
    }
    uiState.hojaAceptar?.let { hoja ->
        HojaAceptar(
            hoja = hoja,
            onElegirMinutos = onElegirMinutos,
            onCerrar = onCerrarHoja,
            onConfirmar = onConfirmarAceptar,
        )
    }
}

@Composable
private fun ListaPedidos(uiState: PedidosUiState, onReintentar: () -> Unit, onAceptar: (Pedido) -> Unit) {
    val modifier = Modifier.fillMaxSize()
    val error = uiState.error
    when {
        error != null -> Column(
            modifier = modifier.padding(24.dp),
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
        uiState.cargando -> Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        else -> LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (uiState.sinConexion) {
                item { AvisoSinConexion() }
            }
            items(uiState.nuevos, key = { it.id }) { pedido ->
                TarjetaPedidoNuevo(pedido = pedido, onAceptar = { onAceptar(pedido) })
            }
            item {
                SeccionPedidos(
                    titulo = R.string.pedidos_seccion_preparacion,
                    vacio = R.string.pedidos_vacio_preparacion,
                    pedidos = uiState.enPreparacion,
                ) { pedido ->
                    val hora = pedido.listoAprox?.let { horaLima(it) }
                    if (hora == null) resumen(pedido.items)
                    else stringResource(R.string.pedidos_listo_aprox, resumen(pedido.items), hora)
                }
            }
            item {
                SeccionPedidos(
                    titulo = R.string.pedidos_seccion_listos,
                    vacio = R.string.pedidos_vacio_listos,
                    pedidos = uiState.listos,
                ) { pedido -> stringResource(R.string.pedidos_esperando, resumen(pedido.items)) }
            }
            item {
                SeccionPedidos(
                    titulo = R.string.pedidos_seccion_en_camino,
                    vacio = R.string.pedidos_vacio_en_camino,
                    pedidos = uiState.enCamino,
                ) { pedido -> pedido.direccion ?: resumen(pedido.items) }
            }
        }
    }
}

@Composable
private fun AvisoSinConexion() {
    Text(
        text = stringResource(R.string.pedidos_sin_conexion),
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
private fun TarjetaPedidoNuevo(pedido: Pedido, onAceptar: () -> Unit) {
    val forma = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .border(width = 3.dp, color = MaterialTheme.colorScheme.secondary, shape = forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .semantics { liveRegion = LiveRegionMode.Assertive },
    ) {
        Text(
            text = stringResource(R.string.pedidos_nuevo),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.pedidos_numero_y_comensal, numero(pedido), pedido.comensalNombre),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(
                        R.string.pedidos_modalidad_y_hora,
                        stringResource(pedido.modalidad.texto()),
                        horaLima(pedido.creadoEn),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                pedido.items.forEach { item -> LineaItem(item) }
                pedido.direccion?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = textoPago(pedido),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (pedido.pago == EstadoPago.PAGADO) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(text = precioTexto(pedido.total), style = MaterialTheme.typography.titleMedium)
            }
            Button(
                onClick = onAceptar,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(48.dp),
            ) {
                Text(text = stringResource(R.string.pedidos_aceptar), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaAceptar(
    hoja: HojaAceptar,
    onElegirMinutos: (Int) -> Unit,
    onCerrar: () -> Unit,
    onConfirmar: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline) },
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(text = stringResource(R.string.pedidos_hoja_aceptar_titulo), style = MaterialTheme.typography.headlineSmall)
            Text(
                text = stringResource(R.string.pedidos_hoja_aceptar_texto, hoja.pedido.comensalNombre),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TIEMPOS_ESTIMADOS.forEach { minutos ->
                    OpcionMinutos(
                        minutos = minutos,
                        elegido = minutos == hoja.minutos,
                        habilitado = !hoja.enviando,
                        onElegir = { onElegirMinutos(minutos) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            hoja.error?.let {
                Text(
                    text = stringResource(it.mensaje()),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCerrar, modifier = Modifier.height(48.dp)) {
                    Text(text = stringResource(R.string.pedidos_volver), style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = onConfirmar,
                    enabled = !hoja.enviando,
                    modifier = Modifier.weight(1f).height(48.dp),
                ) {
                    if (hoja.enviando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = LocalContentColor.current,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        val listoA = Date(System.currentTimeMillis() + hoja.minutos * 60_000L)
                        Text(
                            text = stringResource(R.string.pedidos_aceptar_listo, horaLima(listoA)),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OpcionMinutos(
    minutos: Int,
    elegido: Boolean,
    habilitado: Boolean,
    onElegir: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val forma = RoundedCornerShape(14.dp)
    val contenido = if (elegido) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
    val descripcion = stringResource(R.string.pedidos_minutos_descripcion, minutos)
    Column(
        modifier = modifier
            .height(56.dp)
            .clip(forma)
            .border(
                1.5.dp,
                if (elegido) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                forma,
            )
            .background(if (elegido) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceContainerLowest)
            .selectable(selected = elegido, enabled = habilitado, role = Role.RadioButton, onClick = onElegir)
            .semantics { contentDescription = descripcion },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = minutos.toString(), style = MaterialTheme.typography.titleMedium, color = contenido)
        Text(text = stringResource(R.string.pedidos_minutos), style = MaterialTheme.typography.labelSmall, color = contenido)
    }
}

@Composable
private fun LineaItem(item: ItemPedido) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.pedidos_item, item.cantidad, item.nombre),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (item.entrada != null && item.segundo != null) {
            Text(
                text = stringResource(R.string.pedidos_opciones_menu, item.entrada, item.segundo),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SeccionPedidos(
    @StringRes titulo: Int,
    @StringRes vacio: Int,
    pedidos: List<Pedido>,
    detalle: @Composable (Pedido) -> String,
) {
    Column {
        Text(
            text = stringResource(titulo),
            modifier = Modifier.padding(bottom = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (pedidos.isEmpty()) {
            Text(
                text = stringResource(vacio),
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        pedidos.forEach { pedido -> FilaPedido(pedido = pedido, detalle = detalle(pedido)) }
    }
}

@Composable
private fun FilaPedido(pedido: Pedido, detalle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.pedidos_numero, numero(pedido)),
                style = MaterialTheme.typography.titleSmall,
            )
            ChipModalidad(pedido.modalidad)
            Text(
                text = pedido.comensalNombre,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(text = detalle, style = MaterialTheme.typography.bodySmall)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun ChipModalidad(modalidad: Modalidad) {
    val delivery = modalidad == Modalidad.DELIVERY
    Text(
        text = stringResource(modalidad.texto()),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (delivery) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .padding(horizontal = 7.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall,
        color = if (delivery) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun resumen(items: List<ItemPedido>): String =
    items.map { stringResource(R.string.pedidos_item, it.cantidad, it.nombre) }.joinToString(" + ")

@Composable
private fun textoPago(pedido: Pedido): String {
    val metodo = stringResource(
        when (pedido.metodoPago) {
            MetodoPago.EFECTIVO -> R.string.pedidos_efectivo
            MetodoPago.BILLETERA -> R.string.pedidos_billetera
            MetodoPago.TARJETA -> R.string.pedidos_tarjeta
        }
    )
    return stringResource(
        if (pedido.pago == EstadoPago.PAGADO) R.string.pedidos_pagado else R.string.pedidos_por_cobrar,
        metodo,
    )
}

@StringRes
private fun Modalidad.texto(): Int = when (this) {
    Modalidad.RECOJO -> R.string.pedidos_recojo
    Modalidad.DELIVERY -> R.string.pedidos_delivery
}

@StringRes
private fun ErrorPedidos.mensaje(): Int = when (this) {
    ErrorPedidos.SinConexion -> R.string.error_sin_conexion
    ErrorPedidos.YaCambio -> R.string.error_pedido_ya_cambio
    ErrorPedidos.Desconocido -> R.string.error_desconocido
}

private fun numero(pedido: Pedido): String = "%04d".format(Locale.ROOT, pedido.numero)

private fun horaLima(fecha: Date): String =
    SimpleDateFormat("HH:mm", Locale.ROOT).apply { timeZone = TimeZone.getTimeZone("America/Lima") }.format(fecha)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun BandejaPedidosPreview() {
    val ahora = Date()
    val pedido = Pedido(
        id = "1",
        numero = 132,
        comensalNombre = "Jose C.",
        modalidad = Modalidad.RECOJO,
        direccion = null,
        metodoPago = MetodoPago.BILLETERA,
        pago = EstadoPago.PAGADO,
        items = listOf(
            ItemPedido(cantidad = 1, nombre = "Menú del día", precio = 1400, entrada = "Causa", segundo = "Ají de gallina"),
            ItemPedido(cantidad = 1, nombre = "Lomo saltado", precio = 2400),
        ),
        total = 3800,
        estado = EstadoPedido.RECIBIDO,
        creadoEn = ahora,
        listoAprox = null,
    )
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            BandejaPedidosContenido(
                uiState = PedidosUiState(
                    cargando = false,
                    nuevos = listOf(pedido),
                    enPreparacion = listOf(
                        pedido.copy(id = "2", numero = 129, estado = EstadoPedido.PREPARANDO, listoAprox = ahora)
                    ),
                    enCamino = listOf(
                        pedido.copy(
                            id = "3",
                            numero = 125,
                            modalidad = Modalidad.DELIVERY,
                            direccion = "Facultad de Ingeniería, puerta 3",
                            estado = EstadoPedido.EN_CAMINO,
                        )
                    ),
                ),
                onReintentar = {},
                onAceptar = {},
                onElegirMinutos = {},
                onCerrarHoja = {},
                onConfirmarAceptar = {},
                onAceptadoMostrado = {},
            )
        }
    }
}
