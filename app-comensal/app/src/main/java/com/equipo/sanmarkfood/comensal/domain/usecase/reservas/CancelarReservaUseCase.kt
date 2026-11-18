package com.equipo.sanmarkfood.comensal.domain.usecase.reservas

import com.equipo.sanmarkfood.comensal.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.comensal.domain.repository.RecordatoriosRepository
import com.equipo.sanmarkfood.comensal.domain.repository.ReservasRepository
import javax.inject.Inject

class CancelarReservaUseCase @Inject constructor(
    private val reservasRepository: ReservasRepository,
    private val recordatoriosRepository: RecordatoriosRepository,
) {
    suspend operator fun invoke(reserva: Reserva) {
        if (!reserva.vigente) throw ErrorReservas.EstadoInvalido
        reservasRepository.cancelarReserva(reserva.id)
        recordatoriosRepository.cancelar(reserva.id)
    }
}
