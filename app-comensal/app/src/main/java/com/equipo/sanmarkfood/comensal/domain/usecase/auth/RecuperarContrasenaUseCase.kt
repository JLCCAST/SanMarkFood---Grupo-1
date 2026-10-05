package com.equipo.sanmarkfood.comensal.domain.usecase.auth

import androidx.annotation.StringRes
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.repository.AuthRepository
import javax.inject.Inject

/** Regla: antes de enviar el enlace, el correo no puede estar vacío y debe tener formato válido. */
class RecuperarContrasenaUseCase @Inject constructor(
    private val repositorio: AuthRepository,
    private val validarCorreo: ValidarCorreoUseCase
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank()) return Result.failure(ErrorValidacion(R.string.error_recuperar_correo_vacio))
        if (!validarCorreo(email)) return Result.failure(ErrorValidacion(R.string.error_correo_invalido))

        return repositorio.sendPasswordReset(email.trim())
    }
}

/** Error de validación local, antes de llamar a Firebase. */
class ErrorValidacion(@StringRes val mensajeRes: Int) : Exception()