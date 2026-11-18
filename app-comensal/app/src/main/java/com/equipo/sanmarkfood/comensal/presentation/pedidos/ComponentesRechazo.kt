package com.equipo.sanmarkfood.comensal.presentation.pedidos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.EstadoPago
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MetodoPago
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MotivoRechazo
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.RechazoPedido

@Composable
fun PedidoRechazado(
    pedido: Pedido,
    onVolverALaCarta: () -> Unit,
    onVerOtrosLocales: () -> Unit,
) {
    val rechazo = pedido.rechazo
    val automatico = rechazo == null || rechazo.automatico || rechazo.motivo == MotivoRechazo.SIN_RESPUESTA
    val porPlatoAgotado = rechazo?.motivo == MotivoRechazo.PLATO_AGOTADO
    val forma = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.5.dp, MaterialTheme.colorScheme.error, forma)
            .semantics { liveRegion = LiveRegionMode.Assertive }
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Text(
                text = pedido.actualizadoEn
                    ?.let { stringResource(R.string.rechazo_etiqueta_hora, horaLima(it)) }
                    ?: stringResource(R.string.rechazo_etiqueta),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Text(
            text = stringResource(
                if (automatico) R.string.rechazo_titulo_automatico else R.string.rechazo_titulo,
                pedido.restauranteNombre
            ),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.rechazo_motivo_titulo),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = textoDelMotivo(rechazo, automatico),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            val detalle = rechazo?.detalle
            if (detalle != null && rechazo?.motivo != MotivoRechazo.OTRO) {
                Text(
                    text = detalle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            Text(text = textoDelPago(pedido), style = MaterialTheme.typography.bodyMedium)
        }
    }

    TicketRechazado(pedido)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onVolverALaCarta,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = stringResource(
                    if (porPlatoAgotado) R.string.rechazo_volver_cambiar_plato else R.string.seguimiento_volver_carta
                ),
                fontWeight = FontWeight.SemiBold
            )
        }
        OutlinedButton(
            onClick = onVerOtrosLocales,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(stringResource(R.string.rechazo_ver_otros_locales), fontWeight = FontWeight.SemiBold)
        }
        Text(
            text = stringResource(R.string.rechazo_se_guarda),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun textoDelMotivo(rechazo: RechazoPedido?, automatico: Boolean): String {
    if (rechazo == null || automatico) return stringResource(R.string.rechazo_motivo_sin_respuesta)
    return when (rechazo.motivo) {
        MotivoRechazo.PLATO_AGOTADO -> rechazo.platoAgotado
            ?.let { stringResource(R.string.rechazo_motivo_plato_agotado_nombre, it) }
            ?: stringResource(R.string.rechazo_motivo_plato_agotado)
        MotivoRechazo.COCINA_SATURADA -> stringResource(R.string.rechazo_motivo_cocina_saturada)
        MotivoRechazo.LOCAL_POR_CERRAR -> stringResource(R.string.rechazo_motivo_local_por_cerrar)
        MotivoRechazo.OTRO -> rechazo.detalle ?: stringResource(R.string.rechazo_motivo_otro)
        MotivoRechazo.SIN_RESPUESTA -> stringResource(R.string.rechazo_motivo_sin_respuesta)
    }
}

@Composable
private fun textoDelPago(pedido: Pedido) = buildAnnotatedString {
    if (pedido.pago == EstadoPago.POR_COBRAR) {
        append(stringResource(R.string.rechazo_pago_efectivo))
        return@buildAnnotatedString
    }
    append(stringResource(R.string.rechazo_pago_inicio))
    append(" ")
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
        append(stringResource(R.string.detalle_precio_soles, formatoSoles(pedido.total)))
    }
    append(" ")
    append(
        stringResource(
            if (pedido.metodoPago == MetodoPago.TARJETA) R.string.rechazo_pago_fin_tarjeta
            else R.string.rechazo_pago_fin_billetera
        )
    )
}

@Composable
private fun TicketRechazado(pedido: Pedido) {
    val forma = RoundedCornerShape(14.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.rechazo_tu_pedido),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        pedido.items.forEach { item -> FilaItemRechazado(item, agotado = pedido.seAgoto(item)) }
    }
}

@Composable
private fun FilaItemRechazado(item: ItemPedido, agotado: Boolean) {
    val tachado = if (agotado) TextDecoration.LineThrough else TextDecoration.None
    val color = if (agotado) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.rechazo_item, item.cantidad, item.nombre),
                    style = MaterialTheme.typography.bodyLarge,
                    color = color,
                    textDecoration = tachado
                )
                if (agotado) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = stringResource(R.string.rechazo_agotado),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            val entrada = item.entrada
            val segundo = item.segundo
            if (entrada != null && segundo != null) {
                Text(
                    text = stringResource(R.string.pedido_item_menu_detalle, entrada, segundo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = stringResource(R.string.detalle_precio_soles, formatoSoles(item.precio * item.cantidad)),
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            textDecoration = tachado
        )
    }
}
