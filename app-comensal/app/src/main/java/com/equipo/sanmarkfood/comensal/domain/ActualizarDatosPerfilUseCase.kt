package com.equipo.sanmarkfood.comensal.domain

import com.equipo.sanmarkfood.comensal.data.ComensalRepository
import javax.inject.Inject

/** Regla: el nombre es obligatorio y el teléfono, si se escribe, tiene 9 dígitos. */
class ActualizarDatosPerfilUseCase @Inject constructor(
    private val repository: ComensalRepository
) {
    suspend operator fun invoke(uid: String, nombre: String, telefono: String): Result<Unit> {
        val nombreLimpio = nombre.trim()
        if (nombreLimpio.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre no puede estar vacío"))
        }
        if (telefono.isNotEmpty() && (telefono.length != 9 || !telefono.all(Char::isDigit))) {
            return Result.failure(IllegalArgumentException("El teléfono debe tener 9 dígitos"))
        }
        return repository.actualizarPerfil(uid, nombreLimpio, telefono)
    }
}