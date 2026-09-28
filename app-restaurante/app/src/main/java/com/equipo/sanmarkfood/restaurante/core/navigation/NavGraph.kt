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
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.DatosLocalScreen
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.HorarioLocalScreen

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
        composable<DatosLocal> {
            DatosLocalScreen(
                onContinuar = { navController.navigate(HorarioLocal) },
                onSesionCerrada = { navController.navegarLimpiando(InicioSesion) },
            )
        }
        composable<HorarioLocal> {
            HorarioLocalScreen(
                onVolver = { navController.navigateUp() },
                // Provisional hasta SCRUM-65, que agrega «Local en revisión» (R5).
                onEnviado = { navController.navegarLimpiando(PanelProvisional(administrador = false)) },
            )
        }
    }
}

private fun destinoDe(sesion: EstadoSesion): Any = when (sesion) {
    EstadoSesion.SinSesion -> InicioSesion
    is EstadoSesion.SinVerificar -> VerificarCorreo(sesion.correo)
    EstadoSesion.SinLocal -> DatosLocal
    is EstadoSesion.Activa -> PanelProvisional(administrador = sesion.rol == Rol.ADMINISTRADOR)
}

private fun NavController.navegarLimpiando(ruta: Any) {
    navigate(ruta) {
        popUpTo(graph.id) { inclusive = true }
    }
}
