package com.equipo.sanmarkfood.restaurante.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.equipo.sanmarkfood.restaurante.domain.model.EstadoSesion
import com.equipo.sanmarkfood.restaurante.domain.model.Rol
import com.equipo.sanmarkfood.restaurante.presentation.auth.ArranqueScreen
import com.equipo.sanmarkfood.restaurante.presentation.auth.InicioSesionScreen
import com.equipo.sanmarkfood.restaurante.presentation.auth.PanelProvisionalScreen
import com.equipo.sanmarkfood.restaurante.presentation.auth.RegistroScreen
import com.equipo.sanmarkfood.restaurante.presentation.auth.VerificarCorreoScreen
import com.equipo.sanmarkfood.restaurante.presentation.dashboard.NegocioScreen
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.DatosLocalScreen
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.HorarioLocalScreen
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.ModoFormulario

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
        composable<PanelProvisional> { entrada ->
            PanelProvisionalScreen(
                administrador = entrada.toRoute<PanelProvisional>().administrador,
                onSesionCerrada = { navController.navegarLimpiando(InicioSesion) },
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
                // Al terminar el alta: «Tu negocio» es provisional hasta SCRUM-65, que agrega R5.
                onGuardado = { if (alta) navController.navegarLimpiando(Negocio) else navController.navigateUp() },
                onSalir = { navController.navigateUp() },
            )
        }
        composable<Negocio> {
            NegocioScreen(
                onEditarPerfil = { navController.navigate(DatosLocal(ModoFormulario.EDITAR)) { launchSingleTop = true } },
                onEditarHorario = { navController.navigate(HorarioLocal(ModoFormulario.EDITAR)) { launchSingleTop = true } },
                onSesionCerrada = { navController.navegarLimpiando(InicioSesion) },
            )
        }
    }
}

private fun destinoDe(sesion: EstadoSesion): Any = when (sesion) {
    EstadoSesion.SinSesion -> InicioSesion
    is EstadoSesion.SinVerificar -> VerificarCorreo(sesion.correo)
    EstadoSesion.SinLocal -> DatosLocal(ModoFormulario.ALTA)
    // El local que terminó el alta entra a «Tu negocio» hasta que SCRUM-65 agregue R5 y la barra inferior.
    is EstadoSesion.Activa ->
        if (sesion.rol == Rol.ADMINISTRADOR) PanelProvisional(administrador = true) else Negocio
}

private fun NavController.navegarLimpiando(ruta: Any) {
    navigate(ruta) {
        popUpTo(graph.id) { inclusive = true }
    }
}
