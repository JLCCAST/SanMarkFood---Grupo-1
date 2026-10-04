package com.equipo.sanmarkfood.comensal.domain

import com.equipo.sanmarkfood.comensal.data.ComensalRepository
import javax.inject.Inject

class ActualizarPreferenciasNotificacionUseCase @Inject constructor(
    private val repository: ComensalRepository
) {
    suspend operator fun invoke(
        uid: String,
        notificarReservas: Boolean,
        notificarResenas: Boolean
    ): Result<Unit> =
        repository.actualizarPreferenciasNotificacion(uid, notificarReservas, notificarResenas)
}
