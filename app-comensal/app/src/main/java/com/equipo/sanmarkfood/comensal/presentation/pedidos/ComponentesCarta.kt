package com.equipo.sanmarkfood.comensal.presentation.pedidos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorPedidos
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Plato
import java.util.Locale

internal fun formatoSoles(centimos: Int): String = String.format(Locale.US, "%.2f", centimos / 100.0)

@Composable
fun LaCarta(
    estado: CartaUiState,
    onAgregar: (Plato) -> Unit,
    onCambiarCantidad: (String, Int) -> Unit,
    onReintentar: () -> Unit
) {
    val cantidades = estado.carrito?.items?.associate { it.platoId to it.cantidad }.orEmpty()
    val error = estado.error

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = stringResource(R.string.carta_titulo),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.carta_subtitulo),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    when {
        estado.cargando -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        error != null -> ErrorCarta(error, onReintentar)

        estado.secciones.isEmpty() -> Text(
            text = stringResource(R.string.carta_vacia),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        else -> estado.secciones.forEach { seccion ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = seccion.categoria.nombre.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                seccion.platos.forEach { plato ->
                    FilaPlato(
                        plato = plato,
                        cantidad = cantidades[plato.id] ?: 0,
                        onAgregar = { onAgregar(plato) },
                        onCambiarCantidad = { onCambiarCantidad(plato.id, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaPlato(
    plato: Plato,
    cantidad: Int,
    onAgregar: () -> Unit,
    onCambiarCantidad: (Int) -> Unit
) {
    val agotado = plato.agotadoHoy
    val descripcion = plato.descripcion

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!plato.fotoUrl.isNullOrBlank()) {
            AsyncImage(
                model = plato.fotoUrl,
                contentDescription = plato.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .alpha(if (agotado) 0.5f else 1f)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = plato.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (agotado) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
            if (!descripcion.isNullOrBlank()) {
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(R.string.detalle_precio_soles, formatoSoles(plato.precio)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        when {
            agotado -> Text(
                text = stringResource(R.string.carta_agotado),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            cantidad == 0 -> BotonCircular(
                texto = "+",
                descripcion = stringResource(R.string.carta_agregar, plato.nombre),
                onClick = onAgregar
            )

            else -> Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BotonCircular(
                    texto = "−",
                    descripcion = stringResource(R.string.carta_quitar_uno),
                    onClick = { onCambiarCantidad(cantidad - 1) }
                )
                Text(text = cantidad.toString(), fontWeight = FontWeight.Bold)
                BotonCircular(
                    texto = "+",
                    descripcion = stringResource(R.string.carta_agregar_uno),
                    onClick = { onCambiarCantidad(cantidad + 1) }
                )
            }
        }
    }
}

@Composable
private fun BotonCircular(texto: String, descripcion: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun ErrorCarta(error: ErrorPedidos, onReintentar: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(
                when (error) {
                    ErrorPedidos.SinConexion -> R.string.carta_error_sin_conexion
                    ErrorPedidos.Desconocido -> R.string.carta_error_desconocido
                }
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(onClick = onReintentar) {
            Text(stringResource(R.string.carta_reintentar))
        }
    }
}

@Composable
fun DialogoPedidoNuevo(
    pendiente: PlatoPendiente,
    platosEnPedido: Int,
    onMantener: () -> Unit,
    onVaciar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onMantener,
        title = { Text(stringResource(R.string.pedido_nuevo_titulo)) },
        text = {
            Text(
                pluralStringResource(
                    R.plurals.pedido_nuevo_texto,
                    platosEnPedido,
                    platosEnPedido,
                    pendiente.restauranteActual,
                    pendiente.plato.nombre
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onVaciar) { Text(stringResource(R.string.pedido_nuevo_vaciar)) }
        },
        dismissButton = {
            TextButton(onClick = onMantener) { Text(stringResource(R.string.pedido_nuevo_mantener)) }
        }
    )
}