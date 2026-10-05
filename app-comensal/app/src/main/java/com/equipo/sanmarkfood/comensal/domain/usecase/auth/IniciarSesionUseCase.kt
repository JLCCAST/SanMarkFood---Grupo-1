package com.equipo.sanmarkfood.comensal.domain.usecase.auth

import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.auth.ResultadoInicioSesion
import com.equipo.sanmarkfood.comensal.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Regla: el correo y la contraseña son obligatorios y el correo tiene formato válido.
 * Si pasan, inicia sesión e indica si el correo ya está verificado.
 */
class IniciarSesionUseCase @Inject constructor(
    private val repositorio: AuthRepository,
    private val validarCorreo: ValidarCorreoUseCase
) {
    suspend operator fun invoke(email: String, password: String): ResultadoInicioSesion {
        if (email.isBlank() || password.isEmpty()) {
            return ResultadoInicioSesion.CampoInvalido(R.string.error_login_campos_vacios)
        }
        if (!validarCorreo(email)) {
            return ResultadoInicioSesion.CampoInvalido(R.string.error_correo_invalido)
        }

        return repositorio.login(email.trim(), password).fold(
            onSuccess = { user ->
                if (user.isEmailVerified) {
                    ResultadoInicioSesion.Autenticado
                } else {
                    ResultadoInicioSesion.PorVerificar(user.email ?: email.trim())
                }
            },
            onFailure = { ResultadoInicioSesion.Fallo(it) }
        )
    }
}