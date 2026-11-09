package com.equipo.sanmarkfood.restaurante.domain.usecase.reservas

import com.equipo.sanmarkfood.restaurante.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.EstadoReserva
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.restaurante.domain.repository.ReservasRepository
import javax.inject.Inject

class ConfirmarReservaUseCase @Inject constructor(
    private val reservasRepository: ReservasRepository
) {
    suspend operator fun invoke(reserva: Reserva) {
        if (reserva.estado != EstadoReserva.PENDIENTE) throw ErrorReservas.YaCambio
        reservasRepository.confirmar(reserva.id)
    }
}
