package com.equipo.sanmarkfood.comensal.domain.usecase.auth

import androidx.annotation.StringRes
import com.equipo.sanmarkfood.comensal.R
import javax.inject.Inject

/** Regla: al registrarse, la contraseña debe tener al menos 8 caracteres y un número. */
class ValidarContrasenaRegistroUseCase @Inject constructor() {
    @StringRes
    operator fun invoke(password: String): Int? = when {
        password.length < 8 -> R.string.error_contrasena_longitud
        password.none { it.isDigit() } -> R.string.error_contrasena_numero
        else -> null
    }
}