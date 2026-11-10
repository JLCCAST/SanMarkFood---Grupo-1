package com.equipo.sanmarkfood.comensal.presentation.pedidos

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorCrearPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorUbicacion
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MetodoPago

fun esErrorSinConexion(error: Exception): Boolean =
    error == ErrorCrearPedido.SinConexion || error == ErrorUbicacion.SinConexion

@StringRes
fun mensajeDeErrorEnvio(error: Exception): Int = when (error) {
    ErrorCrearPedido.SesionInvalida -> R.string.envio_error_sesion
    ErrorCrearPedido.SinDireccion -> R.string.envio_error_sin_direccion
    ErrorCrearPedido.LocalNoDisponible -> R.string.envio_error_local_no_disponible
    ErrorCrearPedido.LocalCerrado -> R.string.envio_error_local_cerrado
    ErrorCrearPedido.PlatoAgotado -> R.string.envio_error_plato_agotado
    ErrorCrearPedido.OpcionAgotada -> R.string.envio_error_opcion_agotada
    ErrorCrearPedido.MenuVencido -> R.string.envio_error_menu_vencido
    ErrorUbicacion.NoEncontrada -> R.string.envio_error_direccion_no_encontrada
    ErrorUbicacion.Desconocido -> R.string.envio_error_ubicacion
    else -> R.string.envio_error_desconocido
}

@Composable
fun DialogoSinConexion(onAhoraNo: () -> Unit, onReintentar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onAhoraNo,
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_sin_conexion),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text(stringResource(R.string.envio_sin_conexion_titulo)) },
        text = { Text(stringResource(R.string.envio_sin_conexion_mensaje)) },
        confirmButton = {
            TextButton(onClick = onReintentar) { Text(stringResource(R.string.envio_reintentar)) }
        },
        dismissButton = {
            TextButton(onClick = onAhoraNo) { Text(stringResource(R.string.envio_ahora_no)) }
        }
    )
}

@Composable
fun DialogoErrorEnvio(@StringRes mensaje: Int, onCerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(stringResource(R.string.envio_error_titulo)) },
        text = { Text(stringResource(mensaje)) },
        confirmButton = {
            TextButton(onClick = onCerrar) { Text(stringResource(R.string.envio_entendido)) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaPedidoEnviado(metodoPago: MetodoPago, onSeguirPedido: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onSeguirPedido,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 6.dp)
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(44.dp)
                )
            }
            Text(
                text = stringResource(R.string.enviado_titulo),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(
                    if (metodoPago == MetodoPago.EFECTIVO) R.string.enviado_efectivo else R.string.enviado_pagado
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onSeguirPedido,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(stringResource(R.string.enviado_seguir), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
