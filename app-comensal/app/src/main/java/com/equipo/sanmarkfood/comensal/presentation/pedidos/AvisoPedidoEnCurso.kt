package com.equipo.sanmarkfood.comensal.presentation.pedidos

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.EstadoPedido
import com.equipo.sanmarkfood.comensal.ui.theme.extendedColors

@Composable
fun AvisoPedidoEnCurso(
    onAbrirPedido: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PedidoEnCursoViewModel = hiltViewModel(),
) {
    val estado by viewModel.uiState.collectAsState()
    val pedido = estado.pedido ?: return
    val fijos = MaterialTheme.extendedColors
    val ahi = MaterialTheme.colorScheme.secondary

    val transicion = rememberInfiniteTransition(label = "pedidoEnCurso")
    val alfa by transicion.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alfa"
    )

    Surface(
        onClick = { onAbrirPedido(pedido.id) },
        shape = RoundedCornerShape(20.dp),
        color = fijos.superficieFija,
        shadowElevation = 4.dp,
        modifier = modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
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
                text = stringResource(
                    when (pedido.estado) {
                        EstadoPedido.PREPARANDO -> R.string.aviso_pedido_preparando
                        EstadoPedido.LISTO -> R.string.aviso_pedido_listo
                        EstadoPedido.EN_CAMINO -> R.string.aviso_pedido_en_camino
                        else -> R.string.aviso_pedido_recibido
                    }
                ),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = fijos.sobreSuperficieFija
            )
            Text(
                text = stringResource(R.string.aviso_pedido_ver),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = ahi
            )
        }
    }
}
