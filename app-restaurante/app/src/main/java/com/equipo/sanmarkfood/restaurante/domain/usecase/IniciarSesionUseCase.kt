package com.equipo.sanmarkfood.restaurante.domain.usecase

import com.equipo.sanmarkfood.restaurante.domain.model.ErrorAuth
import com.equipo.sanmarkfood.restaurante.domain.model.EstadoSesion
import com.equipo.sanmarkfood.restaurante.domain.repository.AuthRepository
import javax.inject.Inject

class IniciarSesionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(correo: String, contrasena: String): EstadoSesion {
        if (correo.isBlank() || contrasena.isEmpty()) throw ErrorAuth.CamposVacios
        authRepository.iniciarSesion(correo.trim(), contrasena)
        return authRepository.estadoSesion()
    }
}
