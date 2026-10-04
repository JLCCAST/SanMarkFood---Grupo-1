package com.equipo.sanmarkfood.comensal.domain

import com.equipo.sanmarkfood.comensal.data.ComensalRepository
import com.equipo.sanmarkfood.comensal.data.Direccion
import javax.inject.Inject

/** Regla: una dirección necesita etiqueta y texto. */
class AgregarDireccionUseCase @Inject constructor(
    private val repository: ComensalRepository
) {
    suspend operator fun invoke(
        uid: String,
        actuales: List<Direccion>,
        nueva: Direccion
    ): Result<Unit> {
        if (nueva.etiqueta.isBlank() || nueva.direccionTexto.isBlank()) {
            return Result.failure(IllegalArgumentException("La etiqueta y la dirección son obligatorias"))
        }
        return repository.actualizarDirecciones(uid, actuales + nueva)
    }
}