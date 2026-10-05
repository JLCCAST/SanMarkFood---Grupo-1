package com.equipo.sanmarkfood.restaurante.domain.usecase.admin

import com.equipo.sanmarkfood.restaurante.domain.model.admin.ErrorAdmin
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.MotivoRechazo
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Rechazo
import com.equipo.sanmarkfood.restaurante.domain.repository.RevisionRepository
import javax.inject.Inject

class RechazarLocalUseCase @Inject constructor(
    private val revisionRepository: RevisionRepository
) {
    suspend operator fun invoke(uid: String, motivo: MotivoRechazo?, detalle: String) {
        if (motivo == null) throw ErrorAdmin.MotivoFaltante
        val detalleLimpio = detalle.trim().take(MAX_DETALLE).ifEmpty { null }
        if (motivo == MotivoRechazo.OTRO && (detalleLimpio == null || detalleLimpio.length < MIN_DETALLE_OTRO)) {
            throw ErrorAdmin.DetalleObligatorio
        }
        revisionRepository.rechazar(uid, Rechazo(motivo, detalleLimpio))
    }

    companion object {
        const val MAX_DETALLE = 200
        const val MIN_DETALLE_OTRO = 3
    }
}
