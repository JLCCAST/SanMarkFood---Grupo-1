package com.equipo.sanmarkfood.comensal.domain

/** Regla: al registrarse, la contraseña debe tener al menos 8 caracteres y un número. */
class ValidarContrasenaRegistroUseCase {
    operator fun invoke(password: String): String? = when {
        password.length < 8 -> "La contraseña debe tener al menos 8 caracteres"
        password.none { it.isDigit() } -> "La contraseña debe incluir al menos un número"
        else -> null
    }
}