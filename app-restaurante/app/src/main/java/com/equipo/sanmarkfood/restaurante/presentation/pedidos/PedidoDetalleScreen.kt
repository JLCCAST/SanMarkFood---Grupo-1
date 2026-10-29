package com.equipo.sanmarkfood.restaurante.presentation.pedidos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.EstadoPago
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.EstadoPedido
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.ItemPedido
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.MetodoPago
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.Modalidad
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal
import com.equipo.sanmarkfood.restaurante.presentation.menu.precioTexto
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme
import com.equipo.sanmarkfood.restaurante.ui.theme.Caveat
import java.util.Date

@Composable
fun PedidoDetalleScreen(
    onVolver: () -> Unit,
    onEntregarConCodigo: () -> Unit,
    viewModel: PedidoDetalleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PedidoDetalleContenido(
        uiState = uiState,
        onVolver = onVolver,
        onEntregarConCodigo = onEntregarConCodigo,
        onAvanzar = viewModel::onAvanzar,
        onEntregar = viewModel::onEntregar,
        onPedirSinCodigo = viewModel::onPedirSinCodigo,
        onCancelarSinCodigo = viewModel::onCancelarSinCodigo,
        onConfirmarSinCodigo = viewModel::onConfirmarSinCodigo,
    )
}

@Composable
private fun PedidoDetalleContenido(
    uiState: PedidoDetalleUiState,
    onVolver: () -> Unit,
    onEntregarConCodigo: () -> Unit,
    onAvanzar: () -> Unit,
    onEntregar: () -> Unit,
    onPedirSinCodigo: () -> Unit,
    onCancelarSinCodigo: () -> Unit,
    onConfirmarSinCodigo: () -> Unit,
) {
    val pedido = uiState.pedido
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        BarraDetalle(pedido = pedido, onVolver = onVolver)
        when {
            pedido != null -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    PasosPedido(pedido)
                    Ticket(pedido)
                    pedido.direccion?.let { DireccionDelivery(it) }
                    AvisoFinal(pedido)
                    uiState.error?.let {
                        Text(
                            text = stringResource(it.mensaje()),
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                AccionesDetalle(
                    pedido = pedido,
                    enviando = uiState.enviando,
                    onVolver = onVolver,
                    onEntregarConCodigo = onEntregarConCodigo,
                    onAvanzar = onAvanzar,
                    onEntregar = onEntregar,
                    onPedirSinCodigo = onPedirSinCodigo,
                )
            }
            uiState.cargando -> Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> Text(
                text = stringResource(uiState.error?.mensaje() ?: R.string.detalle_no_encontrado),
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    if (uiState.confirmandoSinCodigo && pedido != null) {
        AlertDialog(
            onDismissRequest = onCancelarSinCodigo,
            title = { Text(text = stringResource(R.string.detalle_sin_codigo_titulo)) },
            text = { Text(text = stringResource(R.string.detalle_sin_codigo_texto, pedido.comensalNombre)) },
            confirmButton = {
                TextButton(onClick = onConfirmarSinCodigo) {
                    Text(text = stringResource(R.string.detalle_si_entregar), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelarSinCodigo) { Text(text = stringResource(R.string.pedidos_volver)) }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        )
    }
}

@Composable
private fun BarraDetalle(pedido: Pedido?, onVolver: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(onClick = onVolver) {
            Icon(painter = painterResource(R.drawable.ic_volver), contentDescription = stringResource(R.string.volver))
        }
        if (pedido != null) {
            Column {
                Text(
                    text = stringResource(R.string.detalle_titulo, numero(pedido)),
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.titleMedium,
                )
                val modalidad = stringResource(
                    if (pedido.modalidad == Modalidad.DELIVERY) R.string.pedidos_delivery else R.string.pedidos_recojo
                )
                Text(
                    text = pedido.listoAprox?.let { stringResource(R.string.detalle_sub_listo, modalidad, horaLima(it)) }
                        ?: modalidad,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PasosPedido(pedido: Pedido) {
    val pasos = listOf(
        R.string.detalle_paso_recibido,
        R.string.detalle_paso_preparando,
        if (pedido.modalidad == Modalidad.DELIVERY) R.string.detalle_paso_en_camino else R.string.detalle_paso_listo,
        R.string.detalle_paso_entregado,
    )
    val actual = when (pedido.estado) {
        EstadoPedido.RECIBIDO -> 0
        EstadoPedido.PREPARANDO -> 1
        EstadoPedido.LISTO, EstadoPedido.EN_CAMINO -> 2
        EstadoPedido.ENTREGADO -> 3
        EstadoPedido.RECHAZADO, EstadoPedido.CANCELADO -> -1
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        pasos.forEachIndexed { indice, paso ->
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (indice <= actual) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        ),
                )
                Text(
                    text = stringResource(paso),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (indice == actual) FontWeight.ExtraBold else null,
                    color = if (indice <= actual) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Ticket(pedido: Pedido) {
    val forma = RoundedCornerShape(14.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = pedido.comensalNombre,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = Caveat),
            )
            Text(
                text = stringResource(R.string.detalle_llego, horaLima(pedido.creadoEn)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            pedido.items.forEach { item -> LineaTicket(item) }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = textoPago(pedido),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = if (pedido.pago == EstadoPago.PAGADO) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = precioTexto(pedido.total), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun LineaTicket(item: ItemPedido) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.pedidos_item, item.cantidad, item.nombre),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(text = precioTexto(item.precio * item.cantidad), style = MaterialTheme.typography.titleSmall)
        }
        if (item.entrada != null && item.segundo != null) {
            Text(
                text = stringResource(R.string.pedidos_opciones_menu, item.entrada, item.segundo),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DireccionDelivery(direccion: String) {
    val forma = RoundedCornerShape(14.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = stringResource(R.string.detalle_entregar_en),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = direccion, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun AvisoFinal(pedido: Pedido) {
    when (pedido.estado) {
        EstadoPedido.ENTREGADO -> AvisoDetalle(
            texto = pedido.entregadoEn?.let { stringResource(R.string.detalle_entregado_a_las, horaLima(it)) }
                ?: stringResource(R.string.detalle_entregado),
            fondo = MaterialTheme.colorScheme.tertiaryContainer,
            contenido = MaterialTheme.colorScheme.onTertiaryContainer,
        )
        EstadoPedido.RECHAZADO -> AvisoDetalle(
            texto = stringResource(R.string.detalle_rechazado),
            fondo = MaterialTheme.colorScheme.errorContainer,
            contenido = MaterialTheme.colorScheme.onErrorContainer,
        )
        EstadoPedido.CANCELADO -> AvisoDetalle(
            texto = stringResource(R.string.detalle_cancelado),
            fondo = MaterialTheme.colorScheme.errorContainer,
            contenido = MaterialTheme.colorScheme.onErrorContainer,
        )
        else -> Unit
    }
}

@Composable
private fun AvisoDetalle(texto: String, fondo: Color, contenido: Color) {
    Text(
        text = texto,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(fondo)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        style = MaterialTheme.typography.labelLarge,
        color = contenido,
    )
}

@Composable
private fun AccionesDetalle(
    pedido: Pedido,
    enviando: Boolean,
    onVolver: () -> Unit,
    onEntregarConCodigo: () -> Unit,
    onAvanzar: () -> Unit,
    onEntregar: () -> Unit,
    onPedirSinCodigo: () -> Unit,
) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (pedido.estado) {
            EstadoPedido.PREPARANDO -> BotonPrincipal(
                texto = stringResource(
                    if (pedido.modalidad == Modalidad.DELIVERY) R.string.detalle_marcar_en_camino
                    else R.string.detalle_marcar_listo
                ),
                onClick = onAvanzar,
                cargando = enviando,
            )
            EstadoPedido.EN_CAMINO -> BotonPrincipal(
                texto = stringResource(R.string.detalle_marcar_entregado),
                onClick = onEntregar,
                cargando = enviando,
            )
            EstadoPedido.LISTO -> {
                Button(
                    onClick = onEntregarConCodigo,
                    enabled = !enviando,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Text(text = stringResource(R.string.pedidos_entregar_con_codigo), style = MaterialTheme.typography.labelLarge)
                }
                TextButton(
                    onClick = onPedirSinCodigo,
                    enabled = !enviando,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                ) {
                    Text(text = stringResource(R.string.detalle_entregar_sin_codigo), style = MaterialTheme.typography.labelLarge)
                }
            }
            EstadoPedido.RECIBIDO -> Unit
            else -> OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurface),
            ) {
                Text(
                    text = stringResource(R.string.detalle_volver_a_pedidos),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PedidoDetallePreview() {
    val pedido = Pedido(
        id = "1",
        numero = 129,
        comensalNombre = "Luis M.",
        modalidad = Modalidad.RECOJO,
        direccion = null,
        metodoPago = MetodoPago.TARJETA,
        pago = EstadoPago.PAGADO,
        items = listOf(
            ItemPedido(cantidad = 2, nombre = "Menú del día", precio = 1400, entrada = "Sopa", segundo = "Ají de gallina"),
            ItemPedido(cantidad = 1, nombre = "Chicha morada 1 L", precio = 800),
        ),
        total = 3600,
        estado = EstadoPedido.LISTO,
        creadoEn = Date(),
        listoAprox = Date(),
    )
    ApprestauranteTheme {
        Surface {
            PedidoDetalleContenido(
                uiState = PedidoDetalleUiState(cargando = false, pedido = pedido),
                onVolver = {},
                onEntregarConCodigo = {},
                onAvanzar = {},
                onEntregar = {},
                onPedirSinCodigo = {},
                onCancelarSinCodigo = {},
                onConfirmarSinCodigo = {},
            )
        }
    }
}
