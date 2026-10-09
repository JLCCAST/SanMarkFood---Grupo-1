package com.equipo.sanmarkfood.restaurante.domain.usecase.auth

import com.equipo.sanmarkfood.restaurante.domain.model.auth.EstadoSesion
import com.equipo.sanmarkfood.restaurante.domain.repository.AuthRepository
import javax.inject.Inject

class IniciarSesionConGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val obtenerEstadoSesion: ObtenerEstadoSesionUseCase,
) {
    suspend operator fun invoke(): EstadoSesion? {
        if (!authRepository.iniciarSesionConGoogle()) return null
        return obtenerEstadoSesion()
    }
}
