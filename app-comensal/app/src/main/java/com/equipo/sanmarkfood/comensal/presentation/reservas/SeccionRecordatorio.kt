package com.equipo.sanmarkfood.comensal.presentation.reservas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            SelectorAnticipacion(anticipacion, onElegir)
        }
    }
}

@Composable
private fun SelectorAnticipacion(
    anticipacion: AnticipacionRecordatorio,
    onElegir: (AnticipacionRecordatorio) -> Unit,
) {
    val colores = MaterialTheme.colorScheme
    val forma = RoundedCornerShape(50)
    val opciones = AnticipacionRecordatorio.entries
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(forma)
            .border(1.dp, colores.outline, forma)
            .selectableGroup()
    ) {
        opciones.forEachIndexed { indice, opcion ->
            if (indice > 0) VerticalDivider(color = colores.outline)
            val elegida = opcion == anticipacion
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .background(if (elegida) colores.inverseSurface else Color.Transparent)
                    .selectable(selected = elegida, role = Role.RadioButton, onClick = { onElegir(opcion) })
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                val estilo = MaterialTheme.typography.labelLarge
                BasicText(
                    text = stringResource(R.string.recordatorio_antes, textoAnticipacion(opcion)),
                    style = estilo.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (elegida) colores.inverseOnSurface else colores.onSurface,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = MaterialTheme.typography.labelSmall.fontSize,
                        maxFontSize = estilo.fontSize
                    )
                )
            }
        }
    }
}
