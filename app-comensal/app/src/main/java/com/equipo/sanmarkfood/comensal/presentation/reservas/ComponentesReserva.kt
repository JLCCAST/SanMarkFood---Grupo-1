package com.equipo.sanmarkfood.comensal.presentation.reservas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.comensal.R

data class FilaTicket(
    val etiqueta: String,
    val valor: String,
    val tachado: Boolean = false,
)

@Composable
fun EtiquetaEstadoReserva(
    texto: String,
    fondo: Color,
    tinta: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = tinta,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(fondo)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
fun LineaPerforada(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.outlineVariant
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = size.height,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
        )
    }
}

@Composable
fun TicketReserva(
    filas: List<FilaTicket>,
    modifier: Modifier = Modifier,
    titulo: String? = null,
    pie: String? = null,
) {
    val forma = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (titulo != null) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            filas.forEach { fila ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = fila.etiqueta,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = fila.valor,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (fila.tachado) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }
        }
        if (pie != null) {
            LineaPerforada(modifier = Modifier.padding(horizontal = 14.dp))
            Text(
                text = pie,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaReservaEnviada(
    enviada: ReservaEnviada,
    pie: String?,
    onListo: () -> Unit,
    onVerActividad: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onListo,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            EtiquetaEstadoReserva(
                texto = stringResource(R.string.reserva_estado_pendiente),
                fondo = MaterialTheme.colorScheme.secondaryContainer,
                tinta = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = stringResource(R.string.reserva_enviada_titulo),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(R.string.reserva_enviada_mensaje, enviada.restauranteNombre),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TicketReserva(
                filas = listOf(
                    FilaTicket(stringResource(R.string.reserva_ticket_fecha), fechaLarga(enviada.fecha)),
                    FilaTicket(stringResource(R.string.reserva_ticket_hora), enviada.hora),
                    FilaTicket(stringResource(R.string.reserva_ticket_comensales), enviada.personas.toString()),
                ),
                pie = pie
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onListo, modifier = Modifier.height(48.dp)) {
                    Text(stringResource(R.string.reserva_enviada_listo), fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onVerActividad,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.reserva_enviada_ver_actividad), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
