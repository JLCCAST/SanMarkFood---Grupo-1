package com.equipo.sanmarkfood.comensal.domain.usecase.reservas

import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.comensal.domain.repository.ReservasRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservarReservaUseCase @Inject constructor(
    private val reservasRepository: ReservasRepository,
) {
    operator fun invoke(reservaId: String): Flow<Reserva?> = reservasRepository.observarReserva(reservaId)
}
