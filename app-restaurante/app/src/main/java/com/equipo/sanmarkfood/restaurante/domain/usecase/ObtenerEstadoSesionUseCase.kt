package com.equipo.sanmarkfood.restaurante.domain.usecase

import com.equipo.sanmarkfood.restaurante.domain.model.ErrorAuth
import com.equipo.sanmarkfood.restaurante.domain.model.EstadoSesion
import com.equipo.sanmarkfood.restaurante.domain.model.Rol
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
        if (sesion is EstadoSesion.Activa && sesion.rol == Rol.RESTAURANTE && !restauranteRepository.tieneLocal()) {
            return EstadoSesion.SinLocal
        }
        return sesion
    }
}
