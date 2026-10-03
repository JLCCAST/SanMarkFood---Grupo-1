package com.equipo.sanmarkfood.comensal.domain

import android.util.Patterns
import javax.inject.Inject

/** Regla: un correo debe tener formato válido. Se usa en registro, login y recuperación. */
class ValidarCorreoUseCase @Inject constructor() {
    operator fun invoke(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
}