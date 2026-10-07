package com.equipo.sanmarkfood.comensal.domain.usecase.auth

import javax.inject.Inject

/** Regla: un correo debe tener formato válido. Se usa en registro, login y recuperación. */
class ValidarCorreoUseCase @Inject constructor() {
    operator fun invoke(email: String): Boolean = FORMATO_CORREO.matches(email.trim())

    private companion object {
        // Kotlin puro: así el caso de uso no depende de Android y se puede probar con tests unitarios.
        val FORMATO_CORREO = Regex(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?" +
                    "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$"
        )
    }
}