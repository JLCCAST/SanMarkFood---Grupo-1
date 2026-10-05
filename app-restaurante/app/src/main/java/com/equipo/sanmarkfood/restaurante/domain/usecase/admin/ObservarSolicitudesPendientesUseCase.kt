package com.equipo.sanmarkfood.restaurante.domain.usecase.admin

import com.equipo.sanmarkfood.restaurante.domain.model.admin.SolicitudLocal
import com.equipo.sanmarkfood.restaurante.domain.repository.SolicitudesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObservarSolicitudesPendientesUseCase @Inject constructor(
    private val solicitudesRepository: SolicitudesRepository
) {
    operator fun invoke(): Flow<List<SolicitudLocal>> =
        solicitudesRepository.observarPendientes().map { solicitudes ->
            solicitudes.sortedByDescending { it.enviadoEn }
        }
}
