package com.equipo.sanmarkfood.restaurante.presentation.panel

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal
import com.equipo.sanmarkfood.restaurante.presentation.dashboard.NegocioScreen
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.EstadoLocal
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.MensajeErrorRestaurante
import com.equipo.sanmarkfood.restaurante.presentation.pedidos.CabeceraPedidos

/**
 * Panel del local con la barra inferior del prototipo: Pedidos · Reservas · Menú · Reseñas · Negocio.
 * Por ahora «Pedidos» muestra el estado del local (R5) y «Negocio» la sección «Tu local» de O6; las
 * demás pestañas muestran un aviso hasta que lleguen HU03 (Menú), HU09 (Pedidos), HU12 y HU21.
 */
@Composable
fun PanelLocalScreen(
    onEditarPerfil: () -> Unit,
    onCorregir: () -> Unit,
    onEditarHorario: () -> Unit,
    onSesionCerrada: () -> Unit,
    viewModel: PanelLocalViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BarraPanel(actual = uiState.pestana, onElegir = viewModel::onElegirPestana) },
    ) { relleno ->
        val contenido = Modifier.fillMaxSize().padding(relleno).consumeWindowInsets(relleno)
        val aprobado = uiState.restaurante?.estado == EstadoRestaurante.APROBADO
        val rechazado = uiState.restaurante?.estado == EstadoRestaurante.RECHAZADO
        when (uiState.pestana) {
            PestanaPanel.PEDIDOS -> PestanaPedidos(
                uiState = uiState,
                onReintentar = viewModel::onReintentar,
                onCambiarRecepcion = viewModel::onCambiarRecepcion,
                onCorregir = onCorregir,
                modifier = contenido,
            )
            PestanaPanel.RESERVAS -> PestanaProvisional(
                icono = R.drawable.ic_reservas,
                texto = stringResource(
                    if (aprobado) R.string.provisional_reservas_aprobado else R.string.provisional_reservas_pendiente
                ),
                modifier = contenido,
            )
            PestanaPanel.MENU -> PestanaProvisional(
                icono = R.drawable.ic_menu,
                texto = stringResource(R.string.provisional_menu),
                modifier = contenido,
            )
            PestanaPanel.RESENAS -> PestanaProvisional(
                icono = R.drawable.ic_resenas,
                texto = stringResource(
                    if (aprobado) R.string.provisional_resenas_aprobado else R.string.provisional_resenas_pendiente
                ),
                modifier = contenido,
            )
            PestanaPanel.NEGOCIO -> NegocioScreen(
                // Rechazado, cambiar los datos es corregirlos: termina en «Guardar y reenviar a revisión» (R7).
                onEditarPerfil = if (rechazado) onCorregir else onEditarPerfil,
                onEditarHorario = onEditarHorario,
                onSesionCerrada = onSesionCerrada,
                modifier = contenido,
            )
        }
    }
}

@Composable
private fun PestanaPedidos(
    uiState: PanelLocalUiState,
    onReintentar: () -> Unit,
    onCambiarRecepcion: (Boolean) -> Unit,
    onCorregir: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val restaurante = uiState.restaurante
    val error = uiState.error
    when {
        error != null -> Column(
            modifier = modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MensajeErrorRestaurante(error = error)
            BotonPrincipal(texto = stringResource(R.string.arranque_reintentar), onClick = onReintentar)
        }
        restaurante == null -> Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        // Solo un local aprobado puede pausar: mientras está en revisión no recibe pedidos de todas formas.
        restaurante.estado == EstadoRestaurante.APROBADO -> Column(modifier = modifier) {
            CabeceraPedidos(
                recibiendo = !restaurante.pausado,
                cambiando = uiState.cambiandoPausa,
                error = uiState.errorPausa,
                onCambiarRecepcion = onCambiarRecepcion,
            )
            EstadoLocal(restaurante = restaurante, onCorregir = onCorregir, modifier = Modifier.weight(1f))
        }
        else -> EstadoLocal(restaurante = restaurante, onCorregir = onCorregir, modifier = modifier)
    }
}

// Aviso de las pestañas que todavía no existen. Cada HU reemplaza la suya.
@Composable
private fun PestanaProvisional(@DrawableRes icono: Int, texto: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(icono),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BarraPanel(actual: PestanaPanel, onElegir: (PestanaPanel) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        PestanaPanel.entries.forEach { pestana ->
            NavigationBarItem(
                selected = pestana == actual,
                onClick = { onElegir(pestana) },
                icon = { Icon(painter = painterResource(pestana.icono), contentDescription = null) },
                // labelMedium del tema es el de las etiquetas en mayúsculas (con espaciado), no sirve aquí.
                label = { Text(text = stringResource(pestana.etiqueta), style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
