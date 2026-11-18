package com.equipo.sanmarkfood.comensal.presentation.reservas

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.reservas.EstadoReserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.claveDiaDe

private val CLAVES = listOf("lun", "mar", "mie", "jue", "vie", "sab", "dom")

@Composable
fun diaYNumero(fecha: String): String {
    val dias = stringArrayResource(R.array.reserva_dias_largos)
    val nombre = dias.getOrElse(CLAVES.indexOf(claveDiaDe(fecha).orEmpty())) { "" }
    val numero = fecha.split("-").getOrNull(2)?.toIntOrNull() ?: 0
    return "$nombre $numero"
}

@Composable
fun DialogoCancelarReserva(
    reserva: Reserva,
    cancelando: Boolean,
    onVolver: () -> Unit,
    onConfirmar: () -> Unit,
) {
    val pendiente = reserva.estado == EstadoReserva.PENDIENTE
    AlertDialog(
        onDismissRequest = { if (!cancelando) onVolver() },
        title = {
            Text(
                stringResource(
                    if (pendiente) R.string.cancelar_solicitud_titulo else R.string.cancelar_reserva_titulo
                )
            )
        },
        text = {
            Text(
                if (pendiente) {
                    stringResource(R.string.cancelar_solicitud_texto, reserva.restauranteNombre)
                } else {
                    stringResource(R.string.cancelar_reserva_texto, diaYNumero(reserva.fecha), reserva.hora)
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmar, enabled = !cancelando) {
                Text(
                    text = stringResource(
                        if (pendiente) R.string.cancelar_solicitud_confirmar else R.string.cancelar_reserva_confirmar
                    ),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onVolver, enabled = !cancelando) {
                Text(stringResource(R.string.cancelar_volver))
            }
        }
    )
}
