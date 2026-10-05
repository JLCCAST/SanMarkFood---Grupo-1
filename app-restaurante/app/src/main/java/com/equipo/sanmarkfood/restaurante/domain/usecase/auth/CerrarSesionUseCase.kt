package com.equipo.sanmarkfood.restaurante.domain.usecase.auth

import com.equipo.sanmarkfood.restaurante.domain.repository.AuthRepository
import com.equipo.sanmarkfood.restaurante.domain.repository.DispositivosRepository
import javax.inject.Inject

class CerrarSesionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val dispositivosRepository: DispositivosRepository,
) {
    operator fun invoke() {
        dispositivosRepository.olvidar()
        authRepository.cerrarSesion()
    }
}
