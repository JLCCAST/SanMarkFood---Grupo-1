package com.equipo.sanmarkfood.comensal.domain.usecase.perfil

import com.equipo.sanmarkfood.comensal.domain.repository.ComensalRepository
import com.equipo.sanmarkfood.comensal.domain.model.perfil.Direccion
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