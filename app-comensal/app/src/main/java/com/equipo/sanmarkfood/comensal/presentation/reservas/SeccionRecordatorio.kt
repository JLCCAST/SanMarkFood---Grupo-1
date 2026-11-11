package com.equipo.sanmarkfood.comensal.presentation.reservas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.reservas.AnticipacionRecordatorio

@Composable
fun textoAnticipacion(anticipacion: AnticipacionRecordatorio): String = stringResource(
    when (anticipacion) {
        AnticipacionRecordatorio.TREINTA_MINUTOS -> R.string.recordatorio_30_min
        AnticipacionRecordatorio.UNA_HORA -> R.string.recordatorio_1_h
        AnticipacionRecordatorio.DOS_HORAS -> R.string.recordatorio_2_h
    }
)

@Composable
fun etiquetaRecordatorio(anticipacion: AnticipacionRecordatorio?): String =
    if (anticipacion == null) stringResource(R.string.recordatorio_ninguno)
    else stringResource(R.string.recordatorio_etiqueta, textoAnticipacion(anticipacion))

@Composable
fun SeccionRecordatorio(
    activo: Boolean,
    anticipacion: AnticipacionRecordatorio,
    onCambiarActivo: (Boolean) -> Unit,
    onElegir: (AnticipacionRecordatorio) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.recordatorio_titulo_seccion),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.recordatorio_detalle_seccion),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = activo, onCheckedChange = onCambiarActivo)
        }
        if (activo) {
            val opciones = AnticipacionRecordatorio.entries
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                opciones.forEachIndexed { indice, opcion ->
                    SegmentedButton(
                        selected = opcion == anticipacion,
                        onClick = { onElegir(opcion) },
                        shape = SegmentedButtonDefaults.itemShape(indice, opciones.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.inverseSurface,
                            activeContentColor = MaterialTheme.colorScheme.inverseOnSurface
                        ),
                        icon = {}
                    ) {
                        Text(
                            text = stringResource(R.string.recordatorio_antes, textoAnticipacion(opcion)),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
