package com.equipo.sanmarkfood.comensal.domain.usecase.auth

import com.equipo.sanmarkfood.comensal.domain.model.auth.ErrorAuth
import com.equipo.sanmarkfood.comensal.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Regla: el correo es obligatorio y tiene formato válido.
 * Si pasa, pide a Firebase que envíe el enlace para crear una contraseña nueva.
 */
class RecuperarContrasenaUseCase @Inject constructor(
    private val repositorio: AuthRepository,
    private val validarCorreo: ValidarCorreoUseCase
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank()) return Result.failure(ErrorAuth.CorreoVacio)
        if (!validarCorreo(email)) return Result.failure(ErrorAuth.CorreoInvalido)

        return repositorio.sendPasswordReset(email.trim()).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it as? ErrorAuth ?: ErrorAuth.Desconocido) }
        )
    }
}