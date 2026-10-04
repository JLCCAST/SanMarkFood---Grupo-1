package com.equipo.sanmarkfood.restaurante.domain.usecase.auth

import com.equipo.sanmarkfood.restaurante.domain.model.auth.ErrorAuth
import com.equipo.sanmarkfood.restaurante.domain.model.auth.EstadoSesion
import com.equipo.sanmarkfood.restaurante.domain.repository.AuthRepository
import javax.inject.Inject

class IniciarSesionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val obtenerEstadoSesion: ObtenerEstadoSesionUseCase,
) {
    suspend operator fun invoke(correo: String, contrasena: String): EstadoSesion {
        if (correo.isBlank() || contrasena.isEmpty()) throw ErrorAuth.CamposVacios
        authRepository.iniciarSesion(correo.trim(), contrasena)
        return obtenerEstadoSesion()
    }
}
