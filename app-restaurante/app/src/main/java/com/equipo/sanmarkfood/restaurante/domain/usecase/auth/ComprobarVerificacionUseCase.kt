package com.equipo.sanmarkfood.restaurante.domain.usecase.auth

import com.equipo.sanmarkfood.restaurante.domain.model.auth.ErrorAuth
import com.equipo.sanmarkfood.restaurante.domain.repository.AuthRepository
import javax.inject.Inject

class ComprobarVerificacionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        if (!authRepository.correoVerificado()) throw ErrorAuth.CorreoSinVerificar
    }
}
