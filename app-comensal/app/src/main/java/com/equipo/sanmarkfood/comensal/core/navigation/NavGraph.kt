package com.equipo.sanmarkfood.comensal.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.equipo.sanmarkfood.comensal.presentation.auth.EstadoSesion
import com.equipo.sanmarkfood.comensal.presentation.auth.InicioSesionScreen
import com.equipo.sanmarkfood.comensal.presentation.auth.InvitadoScreen
import com.equipo.sanmarkfood.comensal.presentation.auth.RegistroScreen
import com.equipo.sanmarkfood.comensal.presentation.auth.SesionViewModel
import com.equipo.sanmarkfood.comensal.presentation.auth.VerificacionCorreoScreen
import com.equipo.sanmarkfood.comensal.presentation.descubrimiento.DetalleRestauranteScreen
import com.equipo.sanmarkfood.comensal.presentation.panel.PanelComensalScreen

private const val RESTAURANTE_A_ENFOCAR = "restauranteAEnfocar"

@Composable
fun ComensalNavGraph(viewModel: SesionViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val sesion by viewModel.estado.collectAsState()
    val inicio = remember { destinoDe(viewModel.estado.value) }

    NavHost(navController = navController, startDestination = inicio) {
        composable<InicioSesion> {
            InicioSesionScreen(
                onIrARegistro = { navController.navigate(Registro) { launchSingleTop = true } },
            )
        }
        composable<Registro> {
            RegistroScreen(
                onIrAInicioSesion = { navController.navegarLimpiando(InicioSesion) },
            )
        }
        composable<VerificarCorreo> { entrada ->
            VerificacionCorreoScreen(correo = entrada.toRoute<VerificarCorreo>().correo)
        }
        composable<Invitado> {
            InvitadoScreen(
                onCrearCuenta = {
                    navController.navegarLimpiando(Registro)
                    viewModel.salirDeInvitado()
                },
                onIniciarSesion = {
                    navController.navegarLimpiando(InicioSesion)
                    viewModel.salirDeInvitado()
                },
            )
        }
        composable<PanelComensal> { entrada ->
            val restauranteAEnfocar by entrada.savedStateHandle
                .getStateFlow<String?>(RESTAURANTE_A_ENFOCAR, null)
                .collectAsState()
            PanelComensalScreen(
                restauranteAEnfocar = restauranteAEnfocar,
                onAbrirRestaurante = { id ->
                    navController.navigate(DetalleRestaurante(id)) { launchSingleTop = true }
                },
                onLogout = viewModel::cerrarSesion,
            )
        }
        composable<DetalleRestaurante> { entrada ->
            val panel = remember(entrada) { navController.getBackStackEntry<PanelComensal>() }
            DetalleRestauranteScreen(
                restauranteId = entrada.toRoute<DetalleRestaurante>().restauranteId,
                onVolver = { navController.navigateUp() },
                onVerEnMapa = { id ->
                    panel.savedStateHandle[RESTAURANTE_A_ENFOCAR] = id
                    navController.navigateUp()
                },
                viewModel = hiltViewModel(panel),
            )
        }
    }

    LaunchedEffect(sesion) {
        val actual = navController.currentDestination ?: return@LaunchedEffect
        if (!actual.correspondeA(sesion)) navController.navegarLimpiando(destinoDe(sesion))
    }
}

private fun destinoDe(sesion: EstadoSesion): Any = when (sesion) {
    EstadoSesion.SinSesion -> InicioSesion
    is EstadoSesion.PorVerificar -> VerificarCorreo(sesion.correo)
    EstadoSesion.Invitado -> Invitado
    EstadoSesion.Autenticado -> PanelComensal
}

private fun NavDestination.correspondeA(sesion: EstadoSesion): Boolean = when (sesion) {
    EstadoSesion.SinSesion -> hasRoute<InicioSesion>() || hasRoute<Registro>()
    is EstadoSesion.PorVerificar -> hasRoute<VerificarCorreo>()
    EstadoSesion.Invitado -> hasRoute<Invitado>()
    EstadoSesion.Autenticado -> hasRoute<PanelComensal>() || hasRoute<DetalleRestaurante>()
}

private fun NavController.navegarLimpiando(ruta: Any) {
    navigate(ruta) {
        popUpTo(graph.id) { inclusive = true }
    }
}
