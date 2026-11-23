package com.equipo.sanmarkfood.restaurante.presentation.dashboard

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.Date
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.CifrasVentas
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.PeriodoNegocio
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.PlatoPedido
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.ResumenNegocio
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.TramoDePedidos
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme

/**
 * O6 «Tu negocio», la pestaña Negocio del panel: los indicadores y el gráfico de pedidos (HU13),
 * solo con el local aprobado, y la sección «Tu local» (HU02).
 */
@Composable
fun NegocioScreen(
    conIndicadores: Boolean,
    onEditarPerfil: () -> Unit,
    onEditarHorario: () -> Unit,
    onSesionCerrada: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NegocioViewModel = hiltViewModel(),
    resumenViewModel: ResumenNegocioViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val resumenState by resumenViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.sesionCerrada) {
        if (uiState.sesionCerrada) onSesionCerrada()
    }

    LaunchedEffect(conIndicadores) {
        if (conIndicadores) resumenViewModel.onAbrir()
    }

    NegocioContenido(
        resumen = resumenState.takeIf { conIndicadores },
        onElegirPeriodo = resumenViewModel::onElegirPeriodo,
        onReintentar = resumenViewModel::onReintentar,
        onEditarPerfil = onEditarPerfil,
        onEditarHorario = onEditarHorario,
        onCerrarSesion = viewModel::onCerrarSesion,
        onEliminarCuenta = viewModel::onPedirEliminacion,
        modifier = modifier,
    )

    if (uiState.confirmandoEliminacion) {
        DialogoEliminarCuenta(
            eliminando = uiState.eliminando,
            error = uiState.errorEliminacion,
            onConfirmar = viewModel::onEliminarCuenta,
            onCancelar = viewModel::onCancelarEliminacion,
        )
    }
}

@Composable
private fun NegocioContenido(
    resumen: ResumenNegocioUiState?,
    onElegirPeriodo: (PeriodoNegocio) -> Unit,
    onReintentar: () -> Unit,
    onEditarPerfil: () -> Unit,
    onEditarHorario: () -> Unit,
    onCerrarSesion: () -> Unit,
    onEliminarCuenta: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Los bordes de la pantalla los maneja el Scaffold del panel.
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.negocio_titulo),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.displaySmall,
            )
            if (resumen != null) SelectorPeriodo(actual = resumen.periodo, onElegir = onElegirPeriodo)
        }

        if (resumen != null) ResumenDelNegocio(uiState = resumen, onReintentar = onReintentar)

        Column {
            Text(
                text = stringResource(R.string.negocio_tu_local),
                modifier = Modifier.padding(bottom = 4.dp),
                style = MaterialTheme.typography.titleSmall,
            )
            FilaEnlace(
                titulo = stringResource(R.string.negocio_perfil),
                ayuda = stringResource(R.string.negocio_perfil_ayuda),
                onClick = onEditarPerfil,
            )
            FilaEnlace(
                titulo = stringResource(R.string.negocio_horario),
                ayuda = stringResource(R.string.negocio_horario_ayuda),
                onClick = onEditarHorario,
            )
            FilaEnlace(
                titulo = stringResource(R.string.negocio_cerrar_sesion),
                ayuda = stringResource(R.string.negocio_cerrar_sesion_ayuda),
                onClick = onCerrarSesion,
                color = MaterialTheme.colorScheme.error,
            )
            FilaEnlace(
                titulo = stringResource(R.string.negocio_eliminar_cuenta),
                ayuda = stringResource(R.string.negocio_eliminar_cuenta_ayuda),
                onClick = onEliminarCuenta,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun DialogoEliminarCuenta(
    eliminando: Boolean,
    @StringRes error: Int?,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(R.string.negocio_eliminar_titulo)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.negocio_eliminar_texto))
                if (error != null) {
                    Text(
                        text = stringResource(error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmar,
                enabled = !eliminando,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(
                    stringResource(if (eliminando) R.string.negocio_eliminar_eliminando else R.string.negocio_eliminar_confirmar)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar, enabled = !eliminando) {
                Text(stringResource(R.string.negocio_eliminar_cancelar))
            }
        },
    )
}

@Composable
private fun FilaEnlace(
    titulo: String,
    ayuda: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
            Text(text = titulo, style = MaterialTheme.typography.labelLarge, color = color)
            Text(
                text = ayuda,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_siguiente),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = color,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun NegocioPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            NegocioContenido(
                resumen = ResumenNegocioUiState(
                    resumenes = mapOf(
                        PeriodoNegocio.SEMANA to ResumenNegocio(
                            periodo = PeriodoNegocio.SEMANA,
                            actual = CifrasVentas(pedidos = 86, centimos = 184_200),
                            anterior = CifrasVentas(pedidos = 74, centimos = 169_000),
                            reservas = 14,
                            reservasAnterior = 16,
                            tramos = listOf(14, 11, 13, 16, 19, 9, 4).map { TramoDePedidos(Date(), it) },
                            tramoActual = 4,
                            platosMasPedidos = listOf(
                                PlatoPedido("Menú del día", 53),
                                PlatoPedido("Lomo saltado", 16),
                                PlatoPedido("Chicharrón de pescado", 8),
                            ),
                        ),
                    ),
                ),
                onElegirPeriodo = {},
                onReintentar = {},
                onEditarPerfil = {},
                onEditarHorario = {},
                onCerrarSesion = {},
                onEliminarCuenta = {},
            )
        }
    }
}
