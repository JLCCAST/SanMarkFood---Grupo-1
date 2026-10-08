package com.equipo.sanmarkfood.restaurante.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.equipo.sanmarkfood.restaurante.domain.model.auth.EstadoSesion
import com.equipo.sanmarkfood.restaurante.domain.model.auth.Rol
import com.equipo.sanmarkfood.restaurante.presentation.admin.PanelAdministradorScreen
import com.equipo.sanmarkfood.restaurante.presentation.admin.aprobacion.RevisarSolicitudScreen
import com.equipo.sanmarkfood.restaurante.presentation.auth.ArranqueScreen
import com.equipo.sanmarkfood.restaurante.presentation.auth.InicioSesionScreen
import com.equipo.sanmarkfood.restaurante.presentation.auth.RegistroScreen
import com.equipo.sanmarkfood.restaurante.presentation.auth.VerificarCorreoScreen
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.DatosLocalScreen
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.HorarioLocalScreen
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.ModoFormulario
import com.equipo.sanmarkfood.restaurante.presentation.menu.ArmarMenuScreen
import com.equipo.sanmarkfood.restaurante.presentation.menu.CamaraScreen
import com.equipo.sanmarkfood.restaurante.presentation.menu.ModoArmarMenu
import com.equipo.sanmarkfood.restaurante.presentation.menu.PlatoScreen
import com.equipo.sanmarkfood.restaurante.presentation.panel.PanelLocalScreen

@Composable
fun RestauranteNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Arranque) {
        composable<Arranque> {
            ArranqueScreen(
                onSesionLeida = { sesion -> navController.navegarLimpiando(destinoDe(sesion)) },
            )
        }
        composable<InicioSesion> {
            InicioSesionScreen(
                onSesionIniciada = { sesion -> navController.navegarLimpiando(destinoDe(sesion)) },
                onRegistrarse = { navController.navigate(Registro) },
            )
        }
        composable<Registro> {
            RegistroScreen(
                onVolver = { navController.navigateUp() },
                onCuentaCreada = { correo -> navController.navegarLimpiando(VerificarCorreo(correo)) },
            )
        }
        composable<VerificarCorreo> {
            VerificarCorreoScreen(
                onVolver = { navController.navegarLimpiando(InicioSesion) },
                onVerificado = { navController.navegarLimpiando(Arranque) },
            )
        }
        composable<PanelAdministrador> {
            PanelAdministradorScreen(
                onAbrirSolicitud = { uid -> navController.navigate(RevisarSolicitud(uid)) { launchSingleTop = true } },
                onSesionCerrada = { navController.navegarLimpiando(InicioSesion) },
            )
        }
        composable<RevisarSolicitud> {
            RevisarSolicitudScreen(
                onVolver = { navController.navigateUp() },
            )
        }
        composable<DatosLocal> { entrada ->
            val alta = entrada.toRoute<DatosLocal>().modo == ModoFormulario.ALTA
            DatosLocalScreen(
                onGuardado = {
                    if (alta) navController.navigate(HorarioLocal(ModoFormulario.ALTA)) else navController.navigateUp()
                },
                onSalir = { navController.navigateUp() },
                onSesionCerrada = { navController.navegarLimpiando(InicioSesion) },
            )
        }
        composable<HorarioLocal> { entrada ->
            val alta = entrada.toRoute<HorarioLocal>().modo == ModoFormulario.ALTA
            HorarioLocalScreen(
                // Al enviar el alta a revisión se abre el panel, con R5 en «Pedidos».
                onGuardado = { if (alta) navController.navegarLimpiando(PanelLocal) else navController.navigateUp() },
                onSalir = { navController.navigateUp() },
            )
        }
        composable<PanelLocal> {
            PanelLocalScreen(
                onEditarPerfil = { navController.navigate(DatosLocal(ModoFormulario.EDITAR)) { launchSingleTop = true } },
                onCorregir = { navController.navigate(DatosLocal(ModoFormulario.CORREGIR)) { launchSingleTop = true } },
                onEditarHorario = { navController.navigate(HorarioLocal(ModoFormulario.EDITAR)) { launchSingleTop = true } },
                onAgregarPlato = { navController.navigate(Plato()) { launchSingleTop = true } },
                onAbrirPlato = { platoId -> navController.navigate(Plato(platoId)) { launchSingleTop = true } },
                onArmarMenu = { modo -> navController.navigate(ArmarMenu(modo)) { launchSingleTop = true } },
                onFotoPizarra = { navController.navigate(Camara) { launchSingleTop = true } },
                onSesionCerrada = { navController.navegarLimpiando(InicioSesion) },
            )
        }
        composable<ArmarMenu> {
            ArmarMenuScreen(
                onCerrar = { navController.navigateUp() },
                onPublicado = { navController.navigateUp() },
            )
        }
        composable<Camara> {
            CamaraScreen(
                onCerrar = { navController.navigateUp() },
                onRevisar = {
                    navController.navigate(ArmarMenu(ModoArmarMenu.IA)) {
                        popUpTo<Camara> { inclusive = true }
                    }
                },
            )
        }
        composable<Plato> {
            PlatoScreen(
                onVolver = { navController.navigateUp() },
                onTerminado = { navController.navigateUp() },
            )
        }
    }
}

private fun destinoDe(sesion: EstadoSesion): Any = when (sesion) {
    EstadoSesion.SinSesion -> InicioSesion
    is EstadoSesion.SinVerificar -> VerificarCorreo(sesion.correo)
    EstadoSesion.SinLocal -> DatosLocal(ModoFormulario.ALTA)
    is EstadoSesion.Activa ->
        if (sesion.rol == Rol.ADMINISTRADOR) PanelAdministrador else PanelLocal
}

private fun NavController.navegarLimpiando(ruta: Any) {
    navigate(ruta) {
        popUpTo(graph.id) { inclusive = true }
    }
}
