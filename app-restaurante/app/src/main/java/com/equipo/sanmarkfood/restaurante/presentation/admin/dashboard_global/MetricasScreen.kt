package com.equipo.sanmarkfood.restaurante.presentation.admin.dashboard_global

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.admin.MetricasPlataforma
import com.equipo.sanmarkfood.restaurante.presentation.admin.CabeceraAdministracion
import com.equipo.sanmarkfood.restaurante.presentation.admin.MensajeErrorAdmin
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun MetricasScreen(
    onSalir: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MetricasViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAbrir()
    }

    MetricasContenido(
        uiState = uiState,
        onSalir = onSalir,
        onReintentar = viewModel::onReintentar,
        modifier = modifier,
    )
}

@Composable
private fun MetricasContenido(
    uiState: MetricasUiState,
    onSalir: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        val actualizadoEn = uiState.actualizadoEn
        CabeceraAdministracion(
            titulo = stringResource(R.string.metricas_titulo),
            subtitulo = if (actualizadoEn != null) {
                stringResource(R.string.metricas_actualizado, horaLima(actualizadoEn))
            } else {
                stringResource(R.string.metricas_subtitulo)
            },
            onSalir = onSalir,
        )

        val metricas = uiState.metricas
        val error = uiState.error
        when {
            metricas != null -> CifrasPlataforma(metricas = metricas, modifier = Modifier.weight(1f))

            uiState.cargando -> Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            error != null -> Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MensajeErrorAdmin(error = error)
                BotonPrincipal(texto = stringResource(R.string.arranque_reintentar), onClick = onReintentar)
            }
        }
    }
}

@Composable
private fun CifrasPlataforma(metricas: MetricasPlataforma, modifier: Modifier = Modifier) {
    val colores = MaterialTheme.colorScheme
    val variacion = metricas.variacionPedidos
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TarjetaCifra(
                etiqueta = stringResource(R.string.metricas_restaurantes_activos),
                valor = metricas.restaurantesActivos,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            TarjetaCifra(
                etiqueta = stringResource(R.string.metricas_pendientes),
                valor = metricas.pendientes,
                detalle = metricas.reenviados.takeIf { it > 0 }
                    ?.let { pluralStringResource(R.plurals.metricas_reenviados, it, it) },
                colorDetalle = colores.onSurfaceVariant,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TarjetaCifra(
                etiqueta = stringResource(R.string.metricas_pedidos_semana),
                valor = metricas.pedidosEstaSemana,
                detalle = variacion?.let { stringResource(R.string.metricas_variacion, it) },
                colorDetalle = if ((variacion ?: 0) < 0) colores.error else colores.tertiary,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            TarjetaCifra(
                etiqueta = stringResource(R.string.metricas_resenas_reportadas),
                valor = metricas.resenasReportadas,
                detalle = if (metricas.resenasReportadas > 0) stringResource(R.string.metricas_por_revisar) else null,
                colorDetalle = colores.error,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun TarjetaCifra(
    etiqueta: String,
    valor: Int,
    modifier: Modifier = Modifier,
    detalle: String? = null,
    colorDetalle: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val forma = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = valor.toString(), style = MaterialTheme.typography.displaySmall)
        if (detalle != null) {
            Text(text = detalle, style = MaterialTheme.typography.labelMedium, color = colorDetalle)
        }
    }
}

private fun horaLima(fecha: Date): String =
    SimpleDateFormat("HH:mm", Locale.ROOT).apply { timeZone = TimeZone.getTimeZone("America/Lima") }.format(fecha)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun MetricasPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            MetricasContenido(
                uiState = MetricasUiState(
                    cargando = false,
                    metricas = MetricasPlataforma(
                        restaurantesActivos = 24,
                        pendientes = 3,
                        reenviados = 1,
                        resenasReportadas = 2,
                        pedidosEstaSemana = 540,
                        pedidosSemanaAnterior = 512,
                    ),
                    actualizadoEn = Date(),
                ),
                onSalir = {},
                onReintentar = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
