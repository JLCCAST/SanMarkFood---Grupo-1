package com.equipo.sanmarkfood.restaurante.presentation.pedidos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.MensajeErrorRestaurante
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme

/**
 * Cabecera de O1 «Pedidos» con el interruptor para pausar los pedidos nuevos (SCRUM-66, decisión R6
 * del diseño). La bandeja de pedidos que va debajo llega con HU09.
 */
@Composable
fun CabeceraPedidos(
    recibiendo: Boolean,
    cambiando: Boolean,
    error: ErrorRestaurante?,
    onCambiarRecepcion: (recibir: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.pedidos_titulo),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                text = stringResource(if (recibiendo) R.string.pedidos_recibiendo else R.string.pedidos_pausado),
                style = MaterialTheme.typography.labelLarge,
                color = if (recibiendo) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val descripcion = stringResource(R.string.pedidos_recibir_descripcion)
            // Verde como en el prototipo: los interruptores de disponibilidad no van en guinda.
            Switch(
                checked = recibiendo,
                onCheckedChange = onCambiarRecepcion,
                modifier = Modifier.semantics { contentDescription = descripcion },
                // Mientras se guarda no se puede volver a tocar: el valor que se ve es el que tiene el servidor.
                enabled = !cambiando,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.tertiary,
                    checkedBorderColor = MaterialTheme.colorScheme.tertiary,
                    checkedThumbColor = MaterialTheme.colorScheme.onTertiary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            )
        }
        if (!recibiendo) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.pedidos_pausado_titulo))
                    }
                    append(" ")
                    append(stringResource(R.string.pedidos_pausado_texto))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        error?.let { MensajeErrorRestaurante(error = it) }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun CabeceraPedidosPausadaPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CabeceraPedidos(recibiendo = false, cambiando = false, error = null, onCambiarRecepcion = {})
        }
    }
}
