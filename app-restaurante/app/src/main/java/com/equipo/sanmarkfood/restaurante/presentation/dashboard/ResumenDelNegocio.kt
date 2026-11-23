package com.equipo.sanmarkfood.restaurante.presentation.dashboard

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.EvolucionCalificacion
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.PeriodoNegocio
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.PlatoPedido
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.ResumenNegocio
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import java.util.Locale

@Composable
fun SelectorPeriodo(actual: PeriodoNegocio, onElegir: (PeriodoNegocio) -> Unit, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(18.dp)
    Row(
        modifier = modifier
            .clip(forma)
            .border(1.dp, MaterialTheme.colorScheme.outline, forma)
            .selectableGroup(),
    ) {
        PeriodoNegocio.entries.forEach { periodo ->
            val elegido = periodo == actual
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .widthIn(min = 64.dp)
                    .then(if (elegido) Modifier.background(MaterialTheme.colorScheme.onSurface) else Modifier)
                    .selectable(selected = elegido, role = Role.Tab, onClick = { onElegir(periodo) })
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(periodo.etiqueta()),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (elegido) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun ResumenDelNegocio(uiState: ResumenNegocioUiState, onReintentar: () -> Unit, modifier: Modifier = Modifier) {
    val resumen = uiState.resumen
    val error = uiState.error
    when {
        resumen != null -> Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Indicadores(resumen = resumen)
            GraficoPedidos(resumen = resumen)
            PlatosMasPedidos(platos = resumen.platosMasPedidos)
            val calificacion = uiState.calificacion
            when {
                calificacion != null -> TarjetaCalificacion(calificacion = calificacion)
                error != null -> ErrorConReintento(error = error, onReintentar = onReintentar)
            }
        }

        error != null -> ErrorConReintento(error = error, onReintentar = onReintentar, modifier = modifier)

        else -> Box(modifier = modifier.fillMaxWidth().height(ALTO_CARGANDO), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ErrorConReintento(error: ErrorRestaurante, onReintentar: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(error.mensajeCifras()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onReintentar) {
            Text(text = stringResource(R.string.arranque_reintentar), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun Indicadores(resumen: ResumenNegocio) {
    val variacionVentas = resumen.variacionVentas
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TarjetaIndicador(
            etiqueta = stringResource(R.string.negocio_pedidos),
            valor = resumen.actual.pedidos.toString(),
            cambio = resumen.diferenciaPedidos,
            detalle = if (resumen.diferenciaPedidos == 0) {
                stringResource(R.string.negocio_sin_cambios)
            } else {
                stringResource(R.string.negocio_vs_anterior, conSigno(resumen.diferenciaPedidos))
            },
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        TarjetaIndicador(
            etiqueta = stringResource(R.string.negocio_ventas),
            valor = stringResource(R.string.precio_soles, solesRedondeados(resumen.actual.centimos)),
            cambio = variacionVentas,
            detalle = when (variacionVentas) {
                null -> null
                0 -> stringResource(R.string.negocio_sin_cambios)
                else -> stringResource(R.string.negocio_porcentaje, conSigno(variacionVentas))
            },
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        TarjetaIndicador(
            etiqueta = stringResource(R.string.negocio_reservas),
            valor = resumen.reservas.toString(),
            cambio = resumen.diferenciaReservas,
            detalle = if (resumen.diferenciaReservas == 0) {
                stringResource(R.string.negocio_sin_cambios)
            } else {
                conSigno(resumen.diferenciaReservas)
            },
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

@Composable
private fun TarjetaIndicador(
    etiqueta: String,
    valor: String,
    cambio: Int?,
    detalle: String?,
    modifier: Modifier = Modifier,
) {
    val colores = MaterialTheme.colorScheme
    val forma = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .clip(forma)
            .background(colores.surfaceContainerLowest)
            .border(1.dp, colores.outlineVariant, forma)
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = etiqueta, style = MaterialTheme.typography.labelMedium, color = colores.onSurfaceVariant)
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(
                minFontSize = MaterialTheme.typography.titleSmall.fontSize,
                maxFontSize = MaterialTheme.typography.headlineSmall.fontSize,
            ),
        )
        if (detalle != null) {
            Text(text = detalle, style = MaterialTheme.typography.labelMedium, color = colorDelCambio(cambio))
        }
    }
}

@Composable
private fun colorDelCambio(cambio: Int?): Color = when {
    cambio == null || cambio == 0 -> MaterialTheme.colorScheme.onSurfaceVariant
    cambio > 0 -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.error
}

@Composable
private fun GraficoPedidos(resumen: ResumenNegocio) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(
                if (resumen.periodo == PeriodoNegocio.SEMANA) R.string.negocio_pedidos_por_dia else R.string.negocio_pedidos_por_semana
            ),
            style = MaterialTheme.typography.titleSmall,
        )
        if (resumen.tramos.all { it.pedidos == 0 }) {
            Text(
                text = stringResource(R.string.negocio_sin_pedidos),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            ColumnasDePedidos(resumen = resumen)
        }
    }
}

@Composable
private fun ColumnasDePedidos(resumen: ResumenNegocio) {
    val colores = MaterialTheme.colorScheme
    val iniciales = stringArrayResource(R.array.negocio_iniciales_dias)
    val etiquetas = if (resumen.periodo == PeriodoNegocio.SEMANA) {
        iniciales.toList()
    } else {
        resumen.tramos.indices.map { stringResource(R.string.negocio_semana_corta, it + 1) }
    }
    val descripcion = stringResource(
        R.string.negocio_grafico_descripcion,
        etiquetas.zip(resumen.tramos) { etiqueta, tramo -> "$etiqueta ${tramo.pedidos}" }.joinToString(", "),
    )
    val formaColumna = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
    val columna = rememberLineComponent(fill = Fill(colores.secondary), thickness = ANCHO_COLUMNA, shape = formaColumna)
    val columnaActual = rememberLineComponent(fill = Fill(colores.primary), thickness = ANCHO_COLUMNA, shape = formaColumna)
    val actual = resumen.tramoActual
    val columnas = remember(columna, columnaActual, actual) {
        object : ColumnCartesianLayer.ColumnProvider {
            override fun getColumn(
                entry: ColumnCartesianLayerModel.Entry,
                seriesIndex: Int,
                extraStore: ExtraStore,
            ): LineComponent = if (entry.x.toInt() == actual) columnaActual else columna

            override fun getWidestSeriesColumn(seriesIndex: Int, extraStore: ExtraStore): LineComponent = columna
        }
    }
    val texto = rememberTextComponent(style = MaterialTheme.typography.labelSmall.copy(color = colores.onSurface))
    val cifras = remember { CartesianValueFormatter { _, valor, _ -> valor.toInt().toString() } }
    val tramosEnEje = remember(etiquetas) {
        CartesianValueFormatter { _, valor, _ -> etiquetas.getOrElse(valor.toInt()) { valor.toInt().toString() } }
    }
    val grafico = rememberCartesianChart(
        rememberColumnCartesianLayer(
            columnProvider = columnas,
            dataLabel = texto,
            dataLabelValueFormatter = cifras,
            rangeProvider = remember(resumen.tramos) {
                CartesianLayerRangeProvider.fixed(
                    minY = 0.0,
                    maxY = resumen.tramos.maxOf { it.pedidos } * ESPACIO_SOBRE_LAS_BARRAS,
                )
            },
        ),
        bottomAxis = HorizontalAxis.rememberBottom(
            line = null,
            label = texto,
            valueFormatter = tramosEnEje,
            tick = null,
            guideline = null,
        ),
    )
    val modelo = remember(resumen.tramos) {
        CartesianChartModel(ColumnCartesianLayerModel.build { series(resumen.tramos.map { it.pedidos }) })
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

@Composable
private fun PlatosMasPedidos(platos: List<PlatoPedido>) {
    val colores = MaterialTheme.colorScheme
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(colores.surfaceContainerLowest)
            .border(1.dp, colores.outlineVariant, forma)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = stringResource(R.string.negocio_platos_mas_pedidos), style = MaterialTheme.typography.titleSmall)
        if (platos.isEmpty()) {
            Text(
                text = stringResource(R.string.negocio_sin_platos),
                style = MaterialTheme.typography.bodyMedium,
                color = colores.onSurfaceVariant,
            )
        }
        val maximo = platos.maxOfOrNull { it.cantidad }?.coerceAtLeast(1) ?: 1
        platos.forEachIndexed { indice, plato ->
            Column(
                modifier = Modifier.semantics(mergeDescendants = true) {},
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = stringResource(R.string.negocio_puesto, indice + 1), style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = plato.nombre,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(text = plato.cantidad.toString(), style = MaterialTheme.typography.labelLarge)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ALTO_BARRA_PLATO)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colores.surfaceContainerHigh),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(plato.cantidad.toFloat() / maximo)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(colores.primary),
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaCalificacion(calificacion: EvolucionCalificacion) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.negocio_calificacion_titulo),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
            )
            val actual = calificacion.actual
            if (actual != null) {
                val cambio = calificacion.cambio
                Text(
                    text = if (cambio == null || cambio == 0.0) {
                        stringResource(R.string.negocio_calificacion_valor, conUnDecimal(actual))
                    } else {
                        pluralStringResource(
                            R.plurals.negocio_calificacion_cambio,
                            calificacion.semanas,
                            conUnDecimal(actual),
                            conSignoDecimal(cambio),
                            calificacion.semanas,
                        )
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = colorDelCambio(cambio?.compareTo(0.0)),
                )
            }
        }
        when {
            calificacion.puntos.isEmpty() -> Text(
                text = stringResource(R.string.negocio_sin_resenas),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            calificacion.puntos.size > 1 -> LineaDeCalificacion(calificacion = calificacion)
        }
    }
}

@Composable
private fun LineaDeCalificacion(calificacion: EvolucionCalificacion) {
    val colores = MaterialTheme.colorScheme
    val valores = calificacion.puntos.map { it.promedio }
    val ultimo = valores.lastIndex
    val descripcion = stringResource(
        R.string.negocio_calificacion_descripcion,
        valores.joinToString(", ") { conUnDecimal(it) },
    )
    val marca = rememberShapeComponent(fill = Fill(colores.secondary), shape = CircleShape)
    val puntos = remember(marca, ultimo) {
        object : LineCartesianLayer.PointProvider {
            private val punto = LineCartesianLayer.Point(marca, TAMANO_PUNTO)

            override fun getPoint(
                entry: LineCartesianLayerModel.Entry,
                seriesIndex: Int,
                extraStore: ExtraStore,
            ): LineCartesianLayer.Point? = if (entry.x.toInt() == ultimo) punto else null

            override fun getLargestPoint(extraStore: ExtraStore): LineCartesianLayer.Point = punto
        }
    }
    val linea = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(colores.secondary)),
        stroke = LineCartesianLayer.LineStroke.Continuous(GROSOR_LINEA),
        areaFill = null,
        pointProvider = puntos,
    )
    val inicio = pluralStringResource(R.plurals.negocio_hace_semanas, calificacion.semanas, calificacion.semanas)
    val hoy = stringResource(R.string.negocio_hoy)
    val enEje = remember(inicio, hoy, ultimo) {
        CartesianValueFormatter { _, valor, _ ->
            when (valor.toInt()) {
                0 -> inicio
                ultimo -> hoy
                else -> conUnDecimal(valor)
            }
        }
    }
    val texto = rememberTextComponent(style = MaterialTheme.typography.labelSmall.copy(color = colores.onSurfaceVariant))
    val grafico = rememberCartesianChart(
        rememberLineCartesianLayer(
            lineProvider = LineCartesianLayer.LineProvider.series(linea),
            rangeProvider = CartesianLayerRangeProvider.fixed(
                minY = (valores.min() - MARGEN_ESTRELLAS).coerceAtLeast(MINIMO_ESTRELLAS),
                maxY = (valores.max() + MARGEN_ESTRELLAS).coerceAtMost(MAXIMO_ESTRELLAS),
            ),
        ),
        bottomAxis = HorizontalAxis.rememberBottom(
            line = null,
            label = texto,
            valueFormatter = enEje,
            tick = null,
            guideline = null,
            itemPlacer = remember(ultimo) { HorizontalAxis.ItemPlacer.aligned(spacing = { ultimo }) },
        ),
    )
    val modelo = remember(valores) { CartesianChartModel(LineCartesianLayerModel.build { series(valores) }) }
    CartesianChartHost(
        chart = grafico,
        model = modelo,
        modifier = Modifier
            .fillMaxWidth()
            .height(ALTO_LINEA)
            .semantics { contentDescription = descripcion },
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content),
    )
}

@Composable
private fun conSignoDecimal(valor: Double): String =
    if (valor > 0) {
        stringResource(R.string.negocio_sube_decimal, conUnDecimal(valor))
    } else {
        stringResource(R.string.negocio_baja_decimal, conUnDecimal(-valor))
    }

private fun conUnDecimal(valor: Double): String = "%.1f".format(Locale.forLanguageTag("es-PE"), valor)

@Composable
private fun conSigno(valor: Int): String =
    if (valor > 0) stringResource(R.string.negocio_sube, valor) else stringResource(R.string.negocio_baja, -valor)

private fun solesRedondeados(centimos: Long): String =
    "%,d".format(Locale.forLanguageTag("es-PE"), (centimos + 50) / 100)

@StringRes
private fun PeriodoNegocio.etiqueta(): Int = when (this) {
    PeriodoNegocio.SEMANA -> R.string.negocio_periodo_semana
    PeriodoNegocio.MES -> R.string.negocio_periodo_mes
}

@StringRes
private fun ErrorRestaurante.mensajeCifras(): Int =
    if (this == ErrorRestaurante.SinConexion) R.string.error_sin_conexion else R.string.negocio_error_cifras

private val ANCHO_COLUMNA = 24.dp
private val ALTO_GRAFICO = 150.dp
private val ALTO_CARGANDO = 120.dp
private val ALTO_LINEA = 90.dp
private val ALTO_BARRA_PLATO = 6.dp
private const val ESPACIO_SOBRE_LAS_BARRAS = 1.25
private val GROSOR_LINEA = 3.dp
private val TAMANO_PUNTO = 10.dp
private const val MARGEN_ESTRELLAS = 0.2
private const val MINIMO_ESTRELLAS = 1.0
private const val MAXIMO_ESTRELLAS = 5.0
