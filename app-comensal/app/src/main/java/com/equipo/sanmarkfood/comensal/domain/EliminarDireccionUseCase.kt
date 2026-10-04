package com.equipo.sanmarkfood.comensal.domain

import com.equipo.sanmarkfood.comensal.data.ComensalRepository
import com.equipo.sanmarkfood.comensal.data.Direccion
import javax.inject.Inject

class EliminarDireccionUseCase @Inject constructor(
    private val repository: ComensalRepository
) {
    suspend operator fun invoke(
        uid: String,
        actuales: List<Direccion>,
        direccion: Direccion
    ): Result<Unit> = repository.actualizarDirecciones(uid, actuales - direccion)
}