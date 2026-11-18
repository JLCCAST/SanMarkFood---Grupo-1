package com.equipo.sanmarkfood.restaurante.presentation.pedidos

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.EstadoPago
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme

@Composable
fun EntregarCodigoScreen(
    onCerrar: () -> Unit,
    viewModel: EntregarCodigoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    EntregarCodigoContenido(
        uiState = uiState,
        onCerrar = onCerrar,
        onDigito = viewModel::onDigito,
        onBorrar = viewModel::onBorrar,
        onEntregar = viewModel::onEntregar,
        onEntregarOtro = viewModel::onEntregarOtro,
    )
}

@Composable
private fun EntregarCodigoContenido(
    uiState: EntregarCodigoUiState,
    onCerrar: () -> Unit,
    onDigito: (Char) -> Unit,
    onBorrar: () -> Unit,
    onEntregar: () -> Unit,
    onEntregarOtro: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onCerrar) {
                Icon(painter = painterResource(R.drawable.ic_cerrar), contentDescription = stringResource(R.string.codigo_cerrar))
            }
            Text(text = stringResource(R.string.pedidos_entregar_con_codigo), style = MaterialTheme.typography.titleMedium)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.codigo_instrucciones),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CasillasCodigo(uiState)
            ResultadoCodigo(uiState = uiState, onEntregar = onEntregar, onEntregarOtro = onEntregarOtro)
            Spacer(modifier = Modifier.weight(1f))
            Teclado(onDigito = onDigito, onBorrar = onBorrar)
        }
    }
}

@Composable
private fun CasillasCodigo(uiState: EntregarCodigoUiState) {
    val descripcion = if (uiState.codigo.isEmpty()) stringResource(R.string.codigo_vacio)
    else stringResource(R.string.codigo_descripcion, uiState.codigo.toList().joinToString(" "))
    Row(
        modifier = Modifier.semantics {
            contentDescription = descripcion
            liveRegion = LiveRegionMode.Polite
        },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(DIGITOS_CODIGO) { indice ->
            val borde = when {
                uiState.entregado != null -> MaterialTheme.colorScheme.outlineVariant
                uiState.encontrado != null -> MaterialTheme.colorScheme.tertiary
                uiState.noEncontrado -> MaterialTheme.colorScheme.error
                indice == uiState.codigo.length -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            val forma = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp)
                    .clip(forma)
                    .border(2.dp, borde, forma)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = uiState.codigo.getOrNull(indice)?.toString().orEmpty(), style = MaterialTheme.typography.displaySmall)
            }
        }
    }
}

@Composable
private fun ResultadoCodigo(uiState: EntregarCodigoUiState, onEntregar: () -> Unit, onEntregarOtro: () -> Unit) {
    val encontrado = uiState.encontrado
    val entregado = uiState.entregado
    when {
        entregado != null -> PedidoEntregado(pedido = entregado, onEntregarOtro = onEntregarOtro)
        encontrado != null -> PedidoEncontrado(
            pedido = encontrado,
            entregando = uiState.entregando,
            onEntregar = onEntregar,
        )
        uiState.noEncontrado -> Aviso(
            texto = stringResource(R.string.codigo_no_encontrado, uiState.codigo),
            fondo = MaterialTheme.colorScheme.errorContainer,
            contenido = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
    uiState.error?.let {
        Text(
            text = stringResource(it.mensaje()),
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun PedidoEncontrado(pedido: Pedido, entregando: Boolean, onEntregar: () -> Unit) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .border(2.dp, MaterialTheme.colorScheme.tertiary, forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.pedidos_numero_y_comensal, numero(pedido), pedido.comensalNombre),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = textoPago(pedido),
                style = MaterialTheme.typography.labelLarge,
                color = if (pedido.pago == EstadoPago.PAGADO) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(text = resumen(pedido.items), style = MaterialTheme.typography.bodyMedium)
        BotonPrincipal(
            texto = stringResource(R.string.codigo_entregar_pedido),
            onClick = onEntregar,
            modifier = Modifier.padding(top = 6.dp),
            cargando = entregando,
        )
    }
}

@Composable
private fun PedidoEntregado(pedido: Pedido, onEntregarOtro: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.codigo_entregado, numero(pedido), pedido.comensalNombre),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
        Button(
            onClick = onEntregarOtro,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                contentColor = MaterialTheme.colorScheme.tertiaryContainer,
            ),
        ) {
            Text(text = stringResource(R.string.codigo_entregar_otro), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun Aviso(texto: String, fondo: Color, contenido: Color) {
    Text(
        text = texto,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(fondo)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics { liveRegion = LiveRegionMode.Assertive },
        style = MaterialTheme.typography.bodyMedium,
        color = contenido,
    )
}

@Composable
private fun Teclado(onDigito: (Char) -> Unit, onBorrar: () -> Unit) {
    val filas = listOf("123", "456", "789")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        filas.forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fila.forEach { digito -> Tecla(modifier = Modifier.weight(1f), onClick = { onDigito(digito) }) { TextoTecla(digito) } }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Spacer(modifier = Modifier.weight(1f))
            Tecla(modifier = Modifier.weight(1f), onClick = { onDigito('0') }) { TextoTecla('0') }
            val borrar = stringResource(R.string.codigo_borrar)
            Tecla(modifier = Modifier.weight(1f), onClick = onBorrar) {
                Icon(painter = painterResource(R.drawable.ic_borrar), contentDescription = borrar)
            }
        }
    }
}

@Composable
private fun Tecla(modifier: Modifier, onClick: () -> Unit, contenido: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        contenido()
    }
}

@Composable
private fun TextoTecla(digito: Char) {
    Text(text = digito.toString(), style = MaterialTheme.typography.titleLarge)
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun EntregarCodigoPreview() {
    ApprestauranteTheme {
        Surface {
            EntregarCodigoContenido(
                uiState = EntregarCodigoUiState(codigo = "48", cargando = false),
                onCerrar = {},
                onDigito = {},
                onBorrar = {},
                onEntregar = {},
                onEntregarOtro = {},
            )
        }
    }
}
