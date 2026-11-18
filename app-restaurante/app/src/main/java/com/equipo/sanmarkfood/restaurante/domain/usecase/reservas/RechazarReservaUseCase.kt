package com.equipo.sanmarkfood.restaurante.domain.usecase.reservas

import com.equipo.sanmarkfood.restaurante.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.EstadoReserva
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.MotivoRechazoReserva
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.restaurante.domain.repository.ReservasRepository
import javax.inject.Inject

class RechazarReservaUseCase @Inject constructor(
    private val reservasRepository: ReservasRepository
) {
    suspend operator fun invoke(reserva: Reserva, motivo: MotivoRechazoReserva, detalle: String) {
        if (reserva.estado != EstadoReserva.PENDIENTE) throw ErrorReservas.YaCambio
        val texto = detalle.trim().takeIf { motivo == MotivoRechazoReserva.OTRO }
        if (motivo == MotivoRechazoReserva.OTRO && (texto == null || texto.length < 3)) throw ErrorReservas.Desconocido
        reservasRepository.rechazar(reserva.id, motivo, texto)
    }
}
