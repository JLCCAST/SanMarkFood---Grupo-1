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
import com.equipo.sanmarkfood.comensal.presentation.pedidos.SeguimientoPedidoScreen
import com.equipo.sanmarkfood.comensal.presentation.pedidos.TuPedidoScreen
import com.equipo.sanmarkfood.comensal.presentation.reservas.ReservarMesaScreen

private const val RESTAURANTE_A_ENFOCAR = "restauranteAEnfocar"
private const val ABRIR_RESERVAS = "abrirReservas"

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
            val abrirReservas by entrada.savedStateHandle
                .getStateFlow(ABRIR_RESERVAS, false)
                .collectAsState()
            PanelComensalScreen(
                restauranteAEnfocar = restauranteAEnfocar,
                abrirReservas = abrirReservas,
                onReservasAbiertas = { entrada.savedStateHandle[ABRIR_RESERVAS] = false },
                onAbrirRestaurante = { id ->
                    navController.navigate(DetalleRestaurante(id)) { launchSingleTop = true }
                },
                onAbrirPedido = { pedidoId ->
                    navController.navigate(SeguimientoPedido(pedidoId)) { launchSingleTop = true }
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
                onVerPedido = { navController.navigate(TuPedido) { launchSingleTop = true } },
                onReservar = { id -> navController.navigate(ReservarMesa(id)) { launchSingleTop = true } },
                viewModel = hiltViewModel(panel),
            )
        }
        composable<TuPedido> {
            TuPedidoScreen(
                onVolver = { navController.navigateUp() },
                onSeguirPedido = { pedidoId ->
                    navController.navigate(SeguimientoPedido(pedidoId)) {
                        popUpTo<PanelComensal>()
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<ReservarMesa> {
            ReservarMesaScreen(
                onVolver = { navController.navigateUp() },
                onVerActividad = {
                    navController.getBackStackEntry<PanelComensal>().savedStateHandle[ABRIR_RESERVAS] = true
                    navController.navigate(PanelComensal) {
                        popUpTo<PanelComensal>()
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<SeguimientoPedido> {
            SeguimientoPedidoScreen(
                onCerrar = {
                    navController.navigate(PanelComensal) {
                        popUpTo<PanelComensal>()
                        launchSingleTop = true
                    }
                },
                onVolverALaCarta = { restauranteId ->
                    navController.navigate(DetalleRestaurante(restauranteId)) {
                        popUpTo<PanelComensal>()
                        launchSingleTop = true
                    }
                },
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
    EstadoSesion.Autenticado -> hasRoute<PanelComensal>() ||
            hasRoute<DetalleRestaurante>() ||
            hasRoute<TuPedido>() ||
            hasRoute<ReservarMesa>() ||
            hasRoute<SeguimientoPedido>()
}

private fun NavController.navegarLimpiando(ruta: Any) {
    navigate(ruta) {
        popUpTo(graph.id) { inclusive = true }
    }
}