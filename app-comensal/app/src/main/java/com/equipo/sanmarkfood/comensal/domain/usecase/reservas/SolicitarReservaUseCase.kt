package com.equipo.sanmarkfood.comensal.domain.usecase.reservas

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import com.equipo.sanmarkfood.comensal.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.SolicitudReserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.validarSolicitud
import com.equipo.sanmarkfood.comensal.domain.repository.ReservasRepository
import javax.inject.Inject

class SolicitarReservaUseCase @Inject constructor(
    private val reservasRepository: ReservasRepository,
) {
    suspend operator fun invoke(
        restaurante: Restaurante,
        fecha: String,
        hora: String,
        personas: Int,
        reservas: List<Reserva>,
        ahora: Long = System.currentTimeMillis(),
    ): String {
        if (!restaurante.aceptaReservas) throw ErrorReservas.SinReservas
        val solicitud = SolicitudReserva(restaurante.id, fecha, hora, personas)
        validarSolicitud(solicitud, restaurante.horario, ahora, reservas)?.let { throw it }
        return reservasRepository.crearReserva(solicitud)
    }
}
