package com.equipo.sanmarkfood.restaurante.domain.usecase.dashboard

import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.EvolucionCalificacion
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.PuntoCalificacion
import com.equipo.sanmarkfood.restaurante.domain.repository.DashboardRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.Date
import javax.inject.Inject

class ObtenerEvolucionCalificacionUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository,
) {
    suspend operator fun invoke(ahora: Date = Date()): EvolucionCalificacion = coroutineScope {
        val puntos = (SEMANAS downTo 0).map { atras ->
            async {
                val hasta = Date(ahora.time - atras * UNA_SEMANA)
                dashboardRepository.promedioDeResenasHasta(hasta)?.let { PuntoCalificacion(hasta, it) }
            }
        }.awaitAll().filterNotNull()

        val semanas = puntos.firstOrNull()?.let { ((ahora.time - it.hasta.time) / UNA_SEMANA).toInt() } ?: 0
        EvolucionCalificacion(puntos = puntos, semanas = semanas)
    }

    private companion object {
        const val SEMANAS = 6
        const val UNA_SEMANA = 7L * 24 * 60 * 60 * 1000
    }
}
