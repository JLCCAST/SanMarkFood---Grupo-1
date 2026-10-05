package com.equipo.sanmarkfood.restaurante.domain.usecase.admin

import com.equipo.sanmarkfood.restaurante.domain.model.admin.FiltroSolicitudes
import com.equipo.sanmarkfood.restaurante.domain.model.admin.SolicitudLocal
import com.equipo.sanmarkfood.restaurante.domain.repository.SolicitudesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObservarSolicitudesUseCase @Inject constructor(
    private val solicitudesRepository: SolicitudesRepository
) {
    operator fun invoke(filtro: FiltroSolicitudes): Flow<List<SolicitudLocal>> =
        solicitudesRepository.observar(filtro.estado).map { solicitudes ->
            if (filtro == FiltroSolicitudes.PENDIENTES) solicitudes.sortedByDescending { it.enviadoEn }
            else solicitudes.sortedByDescending { it.revisadoEn ?: 0L }
        }
}
