package com.equipo.sanmarkfood.comensal.presentation.panel

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.avisos.puedeMostrarAvisos
import com.equipo.sanmarkfood.comensal.presentation.descubrimiento.DescubrimientoScreen
import com.equipo.sanmarkfood.comensal.presentation.historial.ActividadScreen
import com.equipo.sanmarkfood.comensal.presentation.perfil.PerfilScreen

private enum class Pestana(
    @param:StringRes val tituloRes: Int,
    val icono: @Composable () -> Painter
) {
    EXPLORAR(R.string.home_tab_explorar, { painterResource(R.drawable.ic_explorar) }),
    ACTIVIDAD(R.string.home_tab_actividad, { painterResource(R.drawable.ic_actividad) }),
    PERFIL(R.string.home_tab_perfil, { rememberVectorPainter(Icons.Outlined.Person) })
}

@Composable
fun PanelComensalScreen(
    restauranteAEnfocar: String?,
    abrirReservas: Boolean,
    onReservasAbiertas: () -> Unit,
    onAbrirRestaurante: (String) -> Unit,
    onAbrirPedido: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    var seleccionada by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(restauranteAEnfocar) {
        if (restauranteAEnfocar != null) seleccionada = Pestana.EXPLORAR.ordinal
    }
    LaunchedEffect(abrirReservas) {
        if (abrirReservas) seleccionada = Pestana.ACTIVIDAD.ordinal
    }
    PedirPermisoDeAvisos()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            Column {
                HorizontalDivider(thickness = 1.dp, color = colors.outline)
                NavigationBar(containerColor = colors.surface) {
                    Pestana.entries.forEachIndexed { index, pestana ->
                        val titulo = stringResource(pestana.tituloRes)
                        NavigationBarItem(
                            selected = seleccionada == index,
                            onClick = { seleccionada = index },
                            icon = { Icon(painter = pestana.icono(), contentDescription = titulo) },
                            label = { Text(titulo) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.primary,
                                selectedTextColor = colors.primary,
                                indicatorColor = colors.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        when (Pestana.entries[seleccionada]) {
            Pestana.EXPLORAR -> DescubrimientoScreen(
                restauranteAEnfocar = restauranteAEnfocar,
                onRestauranteClick = onAbrirRestaurante,
                onAbrirPedido = onAbrirPedido,
                modifier = Modifier.padding(innerPadding)
            )

            Pestana.ACTIVIDAD -> ActividadScreen(
                abrirReservas = abrirReservas,
                onReservasAbiertas = onReservasAbiertas,
                onVerLocales = { seleccionada = Pestana.EXPLORAR.ordinal },
                modifier = Modifier.padding(innerPadding)
            )

            Pestana.PERFIL -> PerfilScreen(
                onCerrarSesion = onLogout,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun PedirPermisoDeAvisos() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (!puedeMostrarAvisos(context)) pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
