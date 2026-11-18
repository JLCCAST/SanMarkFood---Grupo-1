package com.equipo.sanmarkfood.comensal.domain.usecase.reservas

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.HorarioDia
import com.equipo.sanmarkfood.comensal.domain.model.reservas.AnticipacionRecordatorio
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.validarCambio
import com.equipo.sanmarkfood.comensal.domain.repository.ReservasRepository
import javax.inject.Inject

class ModificarReservaUseCase @Inject constructor(
    private val reservasRepository: ReservasRepository,
    private val programarRecordatorio: ProgramarRecordatorioUseCase,
) {
    suspend operator fun invoke(
        reserva: Reserva,
        fecha: String,
        hora: String,
        horario: Map<String, HorarioDia>,
        recordatorio: AnticipacionRecordatorio?,
        ahora: Long = System.currentTimeMillis(),
    ): AnticipacionRecordatorio? {
        validarCambio(reserva, fecha, hora, horario, ahora)?.let { throw it }
        reservasRepository.modificarReserva(reserva.id, fecha, hora)
        val programado = programarRecordatorio(reserva.id, reserva.restauranteNombre, fecha, hora, recordatorio, ahora)
        return recordatorio.takeIf { programado }
    }
}
