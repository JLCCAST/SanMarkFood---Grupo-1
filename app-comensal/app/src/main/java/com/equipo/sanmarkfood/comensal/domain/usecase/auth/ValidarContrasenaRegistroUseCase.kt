package com.equipo.sanmarkfood.comensal.domain.usecase.auth

import com.equipo.sanmarkfood.comensal.domain.model.auth.ErrorAuth
import javax.inject.Inject

/** Regla: al registrarse, la contraseña debe tener al menos 8 caracteres y un número. */
class ValidarContrasenaRegistroUseCase @Inject constructor() {
    /** Devuelve el error que corresponde, o null si la contraseña cumple las reglas. */
    operator fun invoke(password: String): ErrorAuth? = when {
        password.length < 8 -> ErrorAuth.ContrasenaCorta
        password.none { it.isDigit() } -> ErrorAuth.ContrasenaSinNumero
        else -> null
    }
}