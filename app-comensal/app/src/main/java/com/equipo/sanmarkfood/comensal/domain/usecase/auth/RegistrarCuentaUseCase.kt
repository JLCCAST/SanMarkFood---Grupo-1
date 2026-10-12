package com.equipo.sanmarkfood.comensal.domain.usecase.auth

import com.equipo.sanmarkfood.comensal.domain.model.auth.ErrorAuth
import com.equipo.sanmarkfood.comensal.domain.model.auth.ResultadoRegistro
import com.equipo.sanmarkfood.comensal.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Regla: el nombre es obligatorio, el correo y la contraseña tienen formato válido
 * y hay que aceptar los términos. Si pasan, crea la cuenta.
 */
class RegistrarCuentaUseCase @Inject constructor(
    private val repositorio: AuthRepository,
    private val validarCorreo: ValidarCorreoUseCase,
    private val validarContrasenaRegistro: ValidarContrasenaRegistroUseCase
) {
    suspend operator fun invoke(
        nombre: String,
        email: String,
        password: String,
        aceptoTerminos: Boolean
    ): ResultadoRegistro {
        val problema: ErrorAuth? = when {
            nombre.isBlank() -> ErrorAuth.NombreVacio
            !validarCorreo(email) -> ErrorAuth.CorreoInvalido
            else -> validarContrasenaRegistro(password)
                ?: if (!aceptoTerminos) ErrorAuth.TerminosNoAceptados else null
        }

        if (problema != null) return ResultadoRegistro.CampoInvalido(problema)

        return repositorio.register(nombre.trim(), email.trim(), password).fold(
            onSuccess = { ResultadoRegistro.Registrado(email.trim()) },
            onFailure = { ResultadoRegistro.Fallo(it as? ErrorAuth ?: ErrorAuth.Desconocido) }
        )
    }
}