package com.equipo.sanmarkfood.restaurante.domain.usecase.reservas

import com.equipo.sanmarkfood.restaurante.domain.model.menu.fechaDeHoy
import com.equipo.sanmarkfood.restaurante.domain.model.menu.fechaDentroDe
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.DIAS_VISIBLES
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.EstadoReserva
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.ReservasDeLaSemana
import com.equipo.sanmarkfood.restaurante.domain.repository.ReservasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObservarReservasDeLaSemanaUseCase @Inject constructor(
    private val reservasRepository: ReservasRepository
) {
    operator fun invoke(): Flow<ReservasDeLaSemana> =
        reservasRepository.observarReservasEntre(fechaDeHoy(), fechaDentroDe(DIAS_VISIBLES - 1)).map { semana ->
            semana.copy(
                reservas = semana.reservas.filter {
                    it.estado == EstadoReserva.PENDIENTE || it.estado == EstadoReserva.CONFIRMADA
                }
            )
        }
}
