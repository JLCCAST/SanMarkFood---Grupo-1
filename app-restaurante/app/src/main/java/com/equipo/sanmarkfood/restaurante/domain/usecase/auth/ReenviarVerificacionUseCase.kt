package com.equipo.sanmarkfood.restaurante.domain.usecase.auth

import com.equipo.sanmarkfood.restaurante.domain.repository.AuthRepository
import javax.inject.Inject

class ReenviarVerificacionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() = authRepository.enviarVerificacion()
}
