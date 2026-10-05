package com.equipo.sanmarkfood.restaurante.domain.usecase.auth

import com.equipo.sanmarkfood.restaurante.domain.model.auth.ErrorAuth
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.auth.EstadoSesion
import com.equipo.sanmarkfood.restaurante.domain.model.auth.Rol
import com.equipo.sanmarkfood.restaurante.domain.repository.AuthRepository
import com.equipo.sanmarkfood.restaurante.domain.repository.RestauranteRepository
import javax.inject.Inject

class ObtenerEstadoSesionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val restauranteRepository: RestauranteRepository,
) {
    suspend operator fun invoke(): EstadoSesion {
        val sesion = authRepository.estadoSesion()
        if (sesion is EstadoSesion.Activa && sesion.rol == Rol.COMENSAL) {
            authRepository.cerrarSesion()
            throw ErrorAuth.CuentaDeComensal
        }
        if (sesion is EstadoSesion.Activa && sesion.rol == Rol.RESTAURANTE && !altaTerminada()) {
            return EstadoSesion.SinLocal
        }
        return sesion
    }

    // Sin local o en borrador, el restaurante sigue en el alta (R3 y R4). Los errores se pasan
    // a ErrorAuth porque quienes leen la sesión (arranque e inicio de sesión) muestran esos.
    private suspend fun altaTerminada(): Boolean {
        val local = try {
            restauranteRepository.obtener()
        } catch (e: ErrorRestaurante) {
            throw if (e is ErrorRestaurante.SinConexion) ErrorAuth.SinConexion else ErrorAuth.Desconocido
        }
        return local != null && local.estado != EstadoRestaurante.BORRADOR
    }
}
