package com.equipo.sanmarkfood.restaurante.presentation.admin

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.presentation.admin.aprobacion.SolicitudesScreen
import com.equipo.sanmarkfood.restaurante.presentation.admin.aprobacion.SolicitudesViewModel
import com.equipo.sanmarkfood.restaurante.ui.theme.extendedColors

@Composable
fun PanelAdministradorScreen(
    onSesionCerrada: () -> Unit,
    viewModel: PanelAdministradorViewModel = hiltViewModel(),
    solicitudesViewModel: SolicitudesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val solicitudes by solicitudesViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.sesionCerrada) {
        if (uiState.sesionCerrada) onSesionCerrada()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BarraAdministracion(
                actual = uiState.pestana,
                pendientes = solicitudes.solicitudes.size,
                onElegir = viewModel::onElegirPestana,
            )
        },
    ) { relleno ->
        val contenido = Modifier.fillMaxSize().padding(relleno).consumeWindowInsets(relleno)
        when (uiState.pestana) {
            PestanaAdmin.SOLICITUDES -> SolicitudesScreen(
                onSalir = viewModel::onCerrarSesion,
                modifier = contenido,
                viewModel = solicitudesViewModel,
            )
            PestanaAdmin.REPORTES -> PestanaProvisionalAdmin(
                pestana = PestanaAdmin.REPORTES,
                texto = R.string.admin_reportes_provisional,
                onSalir = viewModel::onCerrarSesion,
                modifier = contenido,
            )
            PestanaAdmin.METRICAS -> PestanaProvisionalAdmin(
                pestana = PestanaAdmin.METRICAS,
                texto = R.string.admin_metricas_provisional,
                onSalir = viewModel::onCerrarSesion,
                modifier = contenido,
            )
        }
    }
}

@Composable
private fun PestanaProvisionalAdmin(
    pestana: PestanaAdmin,
    @StringRes texto: Int,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        CabeceraAdministracion(
            titulo = stringResource(pestana.etiqueta),
            subtitulo = stringResource(texto),
            onSalir = onSalir,
        )
    }
}

@Composable
private fun BarraAdministracion(actual: PestanaAdmin, pendientes: Int, onElegir: (PestanaAdmin) -> Unit) {
    val colores = MaterialTheme.extendedColors
    NavigationBar(containerColor = colores.pizarra) {
        PestanaAdmin.entries.forEach { pestana ->
            NavigationBarItem(
                selected = pestana == actual,
                onClick = { onElegir(pestana) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (pestana == PestanaAdmin.SOLICITUDES && pendientes > 0) {
                                Badge { Text(text = pendientes.toString()) }
                            }
                        },
                    ) {
                        Icon(painter = painterResource(pestana.icono), contentDescription = null)
                    }
                },
                label = { Text(text = stringResource(pestana.etiqueta), style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = colores.sobreSuperficieFija,
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = colores.sobreSuperficieFijaTenue,
                    unselectedTextColor = colores.sobreSuperficieFijaTenue,
                ),
            )
        }
    }
}
