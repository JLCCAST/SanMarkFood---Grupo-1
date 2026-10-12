package com.equipo.sanmarkfood.comensal.domain.usecase.auth

import com.equipo.sanmarkfood.comensal.domain.model.auth.ErrorAuth
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
            return ResultadoInicioSesion.CampoInvalido(ErrorAuth.CamposVacios)
        }
        if (!validarCorreo(email)) {
            return ResultadoInicioSesion.CampoInvalido(ErrorAuth.CorreoInvalido)
        }

        return repositorio.login(email.trim(), password).fold(
            onSuccess = { sesion ->
                if (sesion.correoVerificado) {
                    ResultadoInicioSesion.Autenticado
                } else {
                    ResultadoInicioSesion.PorVerificar(sesion.correo.ifBlank { email.trim() })
                }
            },
            // El repositorio ya traduce Firebase a ErrorAuth; lo demás se trata como desconocido.
            onFailure = { ResultadoInicioSesion.Fallo(it as? ErrorAuth ?: ErrorAuth.Desconocido) }
        )
    }
}