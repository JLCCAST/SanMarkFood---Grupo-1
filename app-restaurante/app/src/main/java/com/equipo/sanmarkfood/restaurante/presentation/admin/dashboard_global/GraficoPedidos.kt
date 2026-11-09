package com.equipo.sanmarkfood.restaurante.presentation.admin.dashboard_global

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.admin.PedidosSemana
import com.equipo.sanmarkfood.restaurante.presentation.admin.fechaCorta
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore

@Composable
fun GraficoPedidosPorSemana(semanas: List<PedidosSemana>, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.metricas_pedidos_por_semana),
            style = MaterialTheme.typography.titleSmall,
        )
        if (semanas.all { it.pedidos == 0 }) {
            Text(
                text = stringResource(R.string.metricas_sin_pedidos),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            ColumnasPorSemana(semanas = semanas)
        }
    }
}

@Composable
private fun ColumnasPorSemana(semanas: List<PedidosSemana>) {
    val colorActual = MaterialTheme.colorScheme.onSurface
    val etiquetas = semanas.map { fechaCorta(it.inicio.time) }
    val descripcion = stringResource(
        R.string.metricas_grafico_descripcion,
        etiquetas.zip(semanas) { etiqueta, semana -> "$etiqueta ${semana.pedidos}" }.joinToString(", "),
    )
    val formaColumna = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
    val columna = rememberLineComponent(
        fill = Fill(colorActual.copy(alpha = OPACIDAD_SEMANAS_PASADAS)),
        thickness = ANCHO_COLUMNA,
        shape = formaColumna,
    )
    val columnaActual = rememberLineComponent(fill = Fill(colorActual), thickness = ANCHO_COLUMNA, shape = formaColumna)
    val ultima = semanas.lastIndex
    val columnas = remember(columna, columnaActual, ultima) {
        object : ColumnCartesianLayer.ColumnProvider {
            override fun getColumn(
                entry: ColumnCartesianLayerModel.Entry,
                seriesIndex: Int,
                extraStore: ExtraStore,
            ): LineComponent = if (entry.x.toInt() == ultima) columnaActual else columna

            override fun getWidestSeriesColumn(seriesIndex: Int, extraStore: ExtraStore): LineComponent = columna
        }
    }
    val texto = rememberTextComponent(style = MaterialTheme.typography.labelSmall.copy(color = colorActual))
    val cifras = remember { CartesianValueFormatter { _, valor, _ -> valor.toInt().toString() } }
    val semanasEnEje = remember(etiquetas) {
        CartesianValueFormatter { _, valor, _ -> etiquetas.getOrElse(valor.toInt()) { "" } }
    }
    val grafico = rememberCartesianChart(
        rememberColumnCartesianLayer(
            columnProvider = columnas,
            dataLabel = texto,
            dataLabelValueFormatter = cifras,
        ),
        bottomAxis = HorizontalAxis.rememberBottom(
            line = null,
            label = texto,
            valueFormatter = semanasEnEje,
            tick = null,
            guideline = null,
        ),
    )
    val modelo = remember(semanas) {
        CartesianChartModel(ColumnCartesianLayerModel.build { series(semanas.map { it.pedidos }) })
    }
    CartesianChartHost(
        chart = grafico,
        model = modelo,
        modifier = Modifier
            .fillMaxWidth()
            .height(ALTO_GRAFICO)
            .semantics { contentDescription = descripcion },
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content),
    )
}

private const val OPACIDAD_SEMANAS_PASADAS = 0.4f
private val ANCHO_COLUMNA = 24.dp
private val ALTO_GRAFICO = 170.dp
