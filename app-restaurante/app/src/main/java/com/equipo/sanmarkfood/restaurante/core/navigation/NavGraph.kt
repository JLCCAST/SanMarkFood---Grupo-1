package com.equipo.sanmarkfood.restaurante.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.equipo.sanmarkfood.restaurante.presentation.auth.PanelProvisionalScreen
import com.equipo.sanmarkfood.restaurante.presentation.auth.RegistroScreen
import com.equipo.sanmarkfood.restaurante.presentation.auth.VerificarCorreoScreen

@Composable
fun RestauranteNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Registro) {
        composable<Registro> {
            RegistroScreen(
                onVolver = { navController.navigateUp() },
                onCuentaCreada = { correo ->
                    navController.navigate(VerificarCorreo(correo)) {
                        popUpTo<Registro> { inclusive = true }
                    }
                },
            )
        }
        composable<VerificarCorreo> {
            VerificarCorreoScreen(
                onVolver = { navController.navigateUp() },
                onVerificado = {
                    navController.navigate(PanelProvisional) {
                        popUpTo<VerificarCorreo> { inclusive = true }
                    }
                },
            )
        }
        composable<PanelProvisional> {
            PanelProvisionalScreen()
        }
    }
}
