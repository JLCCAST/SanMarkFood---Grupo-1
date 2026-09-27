package com.equipo.sanmarkfood.restaurante.domain.usecase

import com.equipo.sanmarkfood.restaurante.domain.repository.AuthRepository
import javax.inject.Inject

class ObtenerEstadoSesionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() = authRepository.estadoSesion()
}
