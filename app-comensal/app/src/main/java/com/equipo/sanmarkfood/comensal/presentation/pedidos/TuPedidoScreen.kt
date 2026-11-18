package com.equipo.sanmarkfood.comensal.presentation.pedidos

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MenuDelCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MetodoPago
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Modalidad
import com.equipo.sanmarkfood.comensal.ui.theme.extendedColors

@Composable
fun TuPedidoScreen(
    onVolver: () -> Unit,
    onSeguirPedido: (String) -> Unit,
    viewModel: TuPedidoViewModel = hiltViewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    val carrito = estado.carrito?.takeIf { it.items.isNotEmpty() }

    val pedidoEnviado = estado.pedidoEnviado
    if (pedidoEnviado != null) {
        HojaPedidoEnviado(
            metodoPago = estado.metodoPago,
            onSeguirPedido = { onSeguirPedido(pedidoEnviado.pedidoId) }
        )
    }

    estado.menuRetirado?.let { retirado ->
        DialogoMenuVencido(
            menuRetirado = retirado,
            onVolverALaCarta = {
                viewModel.cerrarMenuRetirado()
                onVolver()
            },
            onSeguirSinMenu = viewModel::cerrarMenuRetirado
        )
    }

    estado.errorEnvio?.let { error ->
        if (esErrorSinConexion(error)) {
            DialogoSinConexion(
                onAhoraNo = viewModel::cerrarErrorEnvio,
                onReintentar = viewModel::solicitarConfirmacion
            )
        } else {
            DialogoErrorEnvio(
                mensaje = mensajeDeErrorEnvio(error),
                onCerrar = viewModel::cerrarErrorEnvio
            )
        }
    }

    if (estado.agregandoDireccion) {
        DialogoNuevaDireccion(
            guardando = estado.guardandoDireccion,
            error = estado.errorDireccion,
            onGuardar = viewModel::guardarDireccion,
            onCancelar = viewModel::cerrarNuevaDireccion
        )
    }

    val actividad = LocalActivity.current as? FragmentActivity
    val textosBiometria = TextosBiometria(
        titulo = stringResource(R.string.pago_biometria_titulo),
        subtitulo = carrito?.let {
            stringResource(R.string.pago_biometria_subtitulo, formatoSoles(it.total), it.restauranteNombre)
        }.orEmpty(),
        cancelar = stringResource(R.string.pago_biometria_cancelar)
    )
    LaunchedEffect(estado.verificacion) {
        val verificacion = estado.verificacion ?: return@LaunchedEffect
        if (actividad == null) {
            viewModel.resultadoBiometria(ResultadoBiometria.CANCELADO)
        } else {
            confirmarConBiometria(actividad, textosBiometria, verificacion, viewModel::resultadoBiometria)
        }
    }

    if (estado.pagoBloqueado) {
        DialogoPagoBloqueado(
            onUsarBloqueo = viewModel::usarBloqueoDePantalla,
            onCancelar = viewModel::cancelarVerificacion
        )
    }

    if (estado.sinBiometria) {
        val contexto = LocalContext.current
        HojaSinBiometria(
            puedeUsarBloqueo = remember { tieneBloqueoDePantalla(contexto) },
            onUsarBloqueo = viewModel::usarBloqueoDePantalla,
            onConfigurar = {
                viewModel.cancelarVerificacion()
                abrirAjustesDeBiometria(contexto)
            },
            onPagarEnEfectivo = viewModel::pagarEnEfectivo,
            onCerrar = viewModel::cancelarVerificacion
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val mensajePagoCancelado = stringResource(R.string.pago_cancelado)
    LaunchedEffect(estado.pagoCancelado) {
        if (estado.pagoCancelado) {
            try {
                snackbarHostState.showSnackbar(mensajePagoCancelado)
            } finally {
                viewModel.pagoCanceladoMostrado()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { BarraSuperior(onVolver) },
        bottomBar = {
            if (carrito != null) {
                PieConfirmar(
                    total = carrito.total,
                    metodoPago = estado.metodoPago,
                    habilitado = estado.puedeConfirmar,
                    enviando = estado.enviando,
                    onConfirmar = viewModel::solicitarConfirmacion
                )
            }
        }
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

            carrito == null -> PedidoVacio(onVolver, Modifier.padding(padding))

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.pedido_como_recibes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OpcionModalidad(
                        titulo = stringResource(R.string.pedido_recojo),
                        detalle = stringResource(R.string.pedido_recojo_detalle),
                        seleccionada = estado.modalidad == Modalidad.RECOJO,
                        onClick = { viewModel.cambiarModalidad(Modalidad.RECOJO) },
                        modifier = Modifier.weight(1f)
                    )
                    OpcionModalidad(
                        titulo = stringResource(R.string.pedido_delivery),
                        detalle = stringResource(R.string.pedido_delivery_detalle),
                        seleccionada = estado.modalidad == Modalidad.DELIVERY,
                        onClick = { viewModel.cambiarModalidad(Modalidad.DELIVERY) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (estado.modalidad == Modalidad.DELIVERY) {
                    SeccionDireccionEntrega(
                        direcciones = estado.direcciones,
                        elegida = estado.direccionElegida,
                        onElegir = viewModel::elegirDireccion,
                        onAgregar = viewModel::abrirNuevaDireccion
                    )
                }

                Ticket(
                    carrito = carrito,
                    delivery = estado.modalidad == Modalidad.DELIVERY,
                    onCambiarCantidad = viewModel::cambiarCantidad
                )

                Text(
                    text = stringResource(R.string.pedido_metodo_pago),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OpcionPago(
                    titulo = stringResource(R.string.pedido_pago_billetera),
                    detalle = stringResource(R.string.pedido_pago_billetera_detalle),
                    seleccionado = estado.metodoPago == MetodoPago.BILLETERA,
                    onClick = { viewModel.cambiarMetodoPago(MetodoPago.BILLETERA) }
                )
                OpcionPago(
                    titulo = stringResource(R.string.pedido_pago_tarjeta),
                    detalle = stringResource(R.string.pedido_pago_tarjeta_detalle),
                    seleccionado = estado.metodoPago == MetodoPago.TARJETA,
                    onClick = { viewModel.cambiarMetodoPago(MetodoPago.TARJETA) }
                )
                OpcionPago(
                    titulo = stringResource(R.string.pedido_pago_efectivo),
                    detalle = stringResource(R.string.pedido_pago_efectivo_detalle),
                    seleccionado = estado.metodoPago == MetodoPago.EFECTIVO,
                    onClick = { viewModel.cambiarMetodoPago(MetodoPago.EFECTIVO) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun BarraSuperior(onVolver: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onVolver) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.detalle_volver)
            )
        }
        Text(
            text = stringResource(R.string.pedido_titulo),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun OpcionModalidad(
    titulo: String,
    detalle: String,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(16.dp)
    val fijos = MaterialTheme.extendedColors
    Column(
        modifier = modifier
            .clip(forma)
            .background(if (seleccionada) fijos.superficieFija else MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = titulo,
            fontWeight = FontWeight.Bold,
            color = if (seleccionada) fijos.sobreSuperficieFija else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = detalle,
            style = MaterialTheme.typography.bodySmall,
            color = if (seleccionada) fijos.sobreSuperficieFijaTenue else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Ticket(carrito: Carrito, delivery: Boolean, onCambiarCantidad: (String, Int) -> Unit) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = carrito.restauranteNombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.pedido_ticket),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        carrito.items.forEach { item ->
            FilaItem(item) { onCambiarCantidad(item.clave, it) }
        }

        FilaTotal(
            etiqueta = stringResource(R.string.pedido_subtotal),
            monto = carrito.total,
            destacado = false
        )
        if (delivery) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.pedido_envio),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.pedido_envio_detalle),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        FilaTotal(
            etiqueta = stringResource(R.string.pedido_total),
            monto = carrito.total,
            destacado = true
        )
    }
}

@Composable
private fun FilaItem(item: ItemCarrito, onCambiarCantidad: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SelectorCantidad(
            cantidad = item.cantidad,
            onMenos = { onCambiarCantidad(item.cantidad - 1) },
            onMas = { onCambiarCantidad(item.cantidad + 1) }
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nombreDelItem(item),
                style = MaterialTheme.typography.bodyMedium
            )
            if (item is MenuDelCarrito) {
                Text(
                    text = stringResource(R.string.pedido_item_menu_detalle, item.entrada, item.segundo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = stringResource(R.string.detalle_precio_soles, formatoSoles(item.subtotal)),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SelectorCantidad(cantidad: Int, onMenos: () -> Unit, onMas: () -> Unit) {
    val forma = RoundedCornerShape(percent = 50)
    val descripcionMenos = stringResource(R.string.carta_quitar_uno)
    val descripcionMas = stringResource(R.string.carta_agregar_uno)
    Row(
        modifier = Modifier
            .clip(forma)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "−",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable(onClick = onMenos)
                .semantics { contentDescription = descripcionMenos }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
        Text(text = cantidad.toString(), fontWeight = FontWeight.Bold)
        Text(
            text = "+",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable(onClick = onMas)
                .semantics { contentDescription = descripcionMas }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun FilaTotal(etiqueta: String, monto: Int, destacado: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            style = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal,
            color = if (destacado) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.detalle_precio_soles, formatoSoles(monto)),
            style = if (destacado) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) FontWeight.ExtraBold else FontWeight.Normal
        )
    }
}

@Composable
private fun OpcionPago(titulo: String, detalle: String, seleccionado: Boolean, onClick: () -> Unit) {
    val forma = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .border(
                width = if (seleccionado) 2.dp else 1.dp,
                color = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = forma
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = seleccionado, onClick = null, modifier = Modifier.padding(8.dp))
        Text(
            text = titulo,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = detalle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

@Composable
private fun PieConfirmar(
    total: Int,
    metodoPago: MetodoPago,
    habilitado: Boolean,
    enviando: Boolean,
    onConfirmar: () -> Unit,
) {
    val sinPagoAhora = metodoPago == MetodoPago.EFECTIVO
    val monto = formatoSoles(total)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onConfirmar,
            enabled = habilitado,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (enviando) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = stringResource(
                        if (sinPagoAhora) R.string.pedido_confirmar else R.string.pedido_confirmar_pagar,
                        monto
                    ),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text = stringResource(
                if (sinPagoAhora) R.string.pedido_aviso_efectivo else R.string.pedido_aviso_biometria
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PedidoVacio(onVolver: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.pedido_vacio),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onVolver) {
            Text(stringResource(R.string.detalle_volver))
        }
    }
}