package com.equipo.sanmarkfood.comensal.presentation.pedidos

import androidx.annotation.StringRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorCancelarPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorPedidos
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.EstadoPago
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.EstadoPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Modalidad
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.comensal.ui.theme.HandwrittenLargeStyle
import com.equipo.sanmarkfood.comensal.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val PASO_ENTREGADO = 3

@Composable
fun SeguimientoPedidoScreen(
    onCerrar: () -> Unit,
    onVolverALaCarta: (String) -> Unit,
    viewModel: SeguimientoPedidoViewModel = hiltViewModel(),
) {
    val estado by viewModel.uiState.collectAsState()
    val pedido = estado.pedido
    val error = estado.error

    if (estado.confirmandoCancelacion && pedido != null) {
        DialogoCancelarPedido(
            pedido = pedido,
            onMantener = viewModel::mantenerPedido,
            onCancelar = viewModel::cancelar
        )
    }

    LaunchedEffect(estado.irALaCarta) {
        val restauranteId = estado.irALaCarta ?: return@LaunchedEffect
        viewModel.navegacionHecha()
        onVolverALaCarta(restauranteId)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val mensajeErrorCancelar = estado.errorCancelar?.let { stringResource(it.mensaje()) }
    LaunchedEffect(estado.errorCancelar) {
        if (mensajeErrorCancelar != null) {
            try {
                snackbarHostState.showSnackbar(mensajeErrorCancelar)
            } finally {
                viewModel.errorCancelarMostrado()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { BarraSeguimiento(pedido, onCerrar) }
    ) { padding ->
        when {
            estado.cargando -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            error != null -> MensajeSeguimiento(
                texto = stringResource(
                    when (error) {
                        ErrorPedidos.SinConexion -> R.string.seguimiento_error_sin_conexion
                        ErrorPedidos.Desconocido -> R.string.seguimiento_error
                    }
                ),
                onReintentar = viewModel::reintentar,
                modifier = Modifier.padding(padding)
            )

            pedido == null -> MensajeSeguimiento(
                texto = stringResource(R.string.seguimiento_no_encontrado),
                onReintentar = null,
                modifier = Modifier.padding(padding)
            )

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (pedido.estado) {
                    EstadoPedido.CANCELADO -> TarjetaPedidoCerrado(
                        etiqueta = stringResource(R.string.seguimiento_cancelado_etiqueta),
                        titulo = stringResource(R.string.seguimiento_cancelado_titulo),
                        mensaje = stringResource(
                            if (pedido.pago == EstadoPago.REVERTIDO) R.string.seguimiento_cancelado_con_reversion
                            else R.string.seguimiento_cancelado_mensaje
                        ),
                        onVolverALaCarta = viewModel::volverALaCarta
                    )

                    EstadoPedido.RECHAZADO -> PedidoRechazado(
                        pedido = pedido,
                        onVolverALaCarta = viewModel::volverALaCarta,
                        onVerOtrosLocales = onCerrar
                    )

                    else -> {
                        PizarraEstado(pedido)
                        val codigo = pedido.codigoRecojo
                        if (pedido.modalidad == Modalidad.RECOJO && codigo != null) TarjetaCodigoRecojo(codigo)
                        if (pedido.modalidad == Modalidad.DELIVERY) TarjetaEntrega(pedido.direccion)
                        LineaDeTiempo(pedido)
                        if (pedido.estado == EstadoPedido.RECIBIDO) {
                            SeccionCancelar(
                                cancelando = estado.cancelando,
                                onCancelar = viewModel::pedirCancelacion
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun BarraSeguimiento(pedido: Pedido?, onCerrar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCerrar) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.seguimiento_cerrar)
            )
        }
        if (pedido != null) {
            Column {
                Text(
                    text = stringResource(R.string.seguimiento_titulo, numeroDePedido(pedido.numero)),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(
                        R.string.seguimiento_subtitulo,
                        stringResource(
                            if (pedido.modalidad == Modalidad.DELIVERY) R.string.seguimiento_delivery
                            else R.string.seguimiento_recojo
                        ),
                        pedido.restauranteNombre
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PizarraEstado(pedido: Pedido) {
    val fijos = MaterialTheme.extendedColors
    val ahi = MaterialTheme.colorScheme.secondary
    val paso = pedido.estado.paso()
    val delivery = pedido.modalidad == Modalidad.DELIVERY
    val forma = RoundedCornerShape(16.dp)

    val transicion = rememberInfiniteTransition(label = "enVivo")
    val alfa by transicion.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alfa"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(fijos.pizarra)
            .border(6.dp, fijos.pizarraMarco, forma)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer { alpha = alfa }
                    .clip(CircleShape)
                    .background(ahi)
            )
            Text(
                text = stringResource(R.string.seguimiento_en_vivo),
                style = MaterialTheme.typography.labelMedium,
                color = fijos.sobreSuperficieFijaTenue
            )
        }
        Text(
            text = stringResource(
                when (pedido.estado) {
                    EstadoPedido.PREPARANDO -> R.string.seguimiento_preparando_titulo
                    EstadoPedido.LISTO -> R.string.seguimiento_listo_titulo
                    EstadoPedido.EN_CAMINO -> R.string.seguimiento_en_camino_titulo
                    EstadoPedido.ENTREGADO -> R.string.seguimiento_entregado_titulo
                    else -> R.string.seguimiento_recibido_titulo
                }
            ),
            style = HandwrittenLargeStyle,
            color = fijos.sobreSuperficieFija
        )
        Text(
            text = subtituloPizarra(pedido, delivery),
            style = MaterialTheme.typography.bodyMedium,
            color = fijos.sobreSuperficieFijaTenue
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(PASO_ENTREGADO + 1) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (i <= paso) ahi else fijos.sobreSuperficieFijaTenue.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
private fun subtituloPizarra(pedido: Pedido, delivery: Boolean): String = when (pedido.estado) {
    EstadoPedido.PREPARANDO -> pedido.listoAprox?.let {
        stringResource(
            if (delivery) R.string.seguimiento_preparando_delivery else R.string.seguimiento_preparando_recojo,
            horaLima(it)
        )
    } ?: stringResource(R.string.seguimiento_preparando_sin_hora)
    EstadoPedido.LISTO -> stringResource(R.string.seguimiento_listo_detalle)
    EstadoPedido.EN_CAMINO -> stringResource(R.string.seguimiento_en_camino_detalle)
    EstadoPedido.ENTREGADO -> stringResource(R.string.seguimiento_entregado_detalle)
    else -> stringResource(R.string.seguimiento_recibido_detalle)
}

@Composable
private fun TarjetaCodigoRecojo(codigo: String) {
    val forma = RoundedCornerShape(14.dp)
    val linea = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, linea, forma)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.seguimiento_codigo_titulo),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.seguimiento_codigo_detalle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Canvas(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(2.dp)
                .fillMaxHeight()
        ) {
            drawLine(
                color = linea,
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, size.height),
                strokeWidth = size.width,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
            )
        }
        Text(
            text = codigo,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun TarjetaEntrega(direccion: String?) {
    val forma = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.seguimiento_entrega_en),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = direccion ?: stringResource(R.string.seguimiento_entrega_sin_direccion),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.seguimiento_entrega_detalle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class PasoSeguimiento(
    val titulo: String,
    val descripcion: String,
    val hora: Long?,
)

@Composable
private fun LineaDeTiempo(pedido: Pedido) {
    val delivery = pedido.modalidad == Modalidad.DELIVERY
    val paso = pedido.estado.paso()
    val pasos = listOf(
        PasoSeguimiento(
            titulo = stringResource(R.string.seguimiento_paso_recibido),
            descripcion = stringResource(R.string.seguimiento_paso_recibido_detalle),
            hora = pedido.creadoEn
        ),
        PasoSeguimiento(
            titulo = stringResource(R.string.seguimiento_paso_preparando),
            descripcion = stringResource(R.string.seguimiento_paso_preparando_detalle),
            hora = pedido.aceptadoEn
        ),
        PasoSeguimiento(
            titulo = stringResource(
                if (delivery) R.string.seguimiento_paso_en_camino else R.string.seguimiento_paso_listo
            ),
            descripcion = stringResource(
                if (delivery) R.string.seguimiento_paso_en_camino_detalle else R.string.seguimiento_paso_listo_detalle
            ),
            hora = pedido.listoAprox
        ),
        PasoSeguimiento(
            titulo = stringResource(R.string.seguimiento_paso_entregado),
            descripcion = stringResource(
                if (delivery) R.string.seguimiento_paso_entregado_delivery else R.string.seguimiento_paso_entregado_recojo
            ),
            hora = pedido.entregadoEn
        ),
    )
    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
        pasos.forEachIndexed { i, datos ->
            FilaPaso(
                datos = datos,
                esPasoActual = i == paso,
                hecho = i < paso || (i == paso && paso == PASO_ENTREGADO),
                alcanzado = i <= paso,
                lineaHecha = i < paso,
                ultimo = i == pasos.lastIndex
            )
        }
    }
}

@Composable
private fun FilaPaso(
    datos: PasoSeguimiento,
    esPasoActual: Boolean,
    hecho: Boolean,
    alcanzado: Boolean,
    lineaHecha: Boolean,
    ultimo: Boolean,
) {
    val colores = MaterialTheme.colorScheme
    val actual = esPasoActual && !hecho
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            hecho -> colores.primary
                            actual -> colores.primaryContainer
                            else -> colores.surface
                        }
                    )
                    .border(2.dp, if (hecho || actual) colores.primary else colores.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (hecho) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = colores.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            if (!ultimo) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(if (lineaHecha) colores.primary else colores.outlineVariant)
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = datos.titulo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (esPasoActual) FontWeight.Bold else FontWeight.Medium,
                    color = if (alcanzado) colores.onSurface else colores.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                val hora = datos.hora
                if (alcanzado && hora != null) {
                    Text(
                        text = horaLima(hora),
                        style = MaterialTheme.typography.bodySmall,
                        color = colores.onSurfaceVariant
                    )
                }
            }
            Text(
                text = datos.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = colores.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SeccionCancelar(cancelando: Boolean, onCancelar: () -> Unit) {
    val rojo = MaterialTheme.colorScheme.error
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OutlinedButton(
            onClick = onCancelar,
            enabled = !cancelando,
            border = BorderStroke(1.5.dp, rojo),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = rojo),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (cancelando) {
                CircularProgressIndicator(color = rojo, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                Text(stringResource(R.string.cancelar_pedido), fontWeight = FontWeight.SemiBold)
            }
        }
        Text(
            text = stringResource(R.string.cancelar_pedido_pista),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DialogoCancelarPedido(pedido: Pedido, onMantener: () -> Unit, onCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onMantener,
        title = { Text(stringResource(R.string.cancelar_pedido_titulo)) },
        text = {
            Text(
                stringResource(
                    if (pedido.pago == EstadoPago.PAGADO) R.string.cancelar_pedido_mensaje_pagado
                    else R.string.cancelar_pedido_mensaje,
                    pedido.restauranteNombre
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = onCancelar,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.cancelar_pedido), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onMantener) { Text(stringResource(R.string.cancelar_pedido_mantener)) }
        }
    )
}

@StringRes
private fun ErrorCancelarPedido.mensaje(): Int = when (this) {
    ErrorCancelarPedido.SinConexion -> R.string.cancelar_pedido_error_sin_conexion
    ErrorCancelarPedido.YaAceptado -> R.string.cancelar_pedido_error_ya_aceptado
    ErrorCancelarPedido.Desconocido -> R.string.cancelar_pedido_error
}

@Composable
private fun TarjetaPedidoCerrado(
    etiqueta: String,
    titulo: String,
    mensaje: String,
    onVolverALaCarta: () -> Unit,
) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onVolverALaCarta,
            modifier = Modifier
                .padding(top = 8.dp)
                .height(48.dp)
        ) {
            Text(stringResource(R.string.seguimiento_volver_carta))
        }
    }
}

@Composable
private fun MensajeSeguimiento(texto: String, onReintentar: (() -> Unit)?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (onReintentar != null) {
            OutlinedButton(onClick = onReintentar, modifier = Modifier.padding(top = 16.dp)) {
                Text(stringResource(R.string.carta_reintentar))
            }
        }
    }
}

private fun EstadoPedido.paso(): Int = when (this) {
    EstadoPedido.PREPARANDO -> 1
    EstadoPedido.LISTO, EstadoPedido.EN_CAMINO -> 2
    EstadoPedido.ENTREGADO -> PASO_ENTREGADO
    else -> 0
}

internal fun numeroDePedido(numero: Int): String = String.format(Locale.ROOT, "%04d", numero)

internal fun horaLima(milisegundos: Long): String =
    SimpleDateFormat("HH:mm", Locale.ROOT)
        .apply { timeZone = TimeZone.getTimeZone("America/Lima") }
        .format(Date(milisegundos))
