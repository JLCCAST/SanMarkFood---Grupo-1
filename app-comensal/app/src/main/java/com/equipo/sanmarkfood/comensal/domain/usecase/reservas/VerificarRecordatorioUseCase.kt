package com.equipo.sanmarkfood.comensal.domain.usecase.reservas

import com.equipo.sanmarkfood.comensal.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.comensal.domain.repository.RecordatoriosRepository
import com.equipo.sanmarkfood.comensal.domain.repository.ReservasRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class VerificarRecordatorioUseCase @Inject constructor(
    private val reservasRepository: ReservasRepository,
    private val recordatoriosRepository: RecordatoriosRepository,
) {
    suspend operator fun invoke(reservaId: String, fecha: String, hora: String): Boolean {
        val lectura = try {
            withTimeoutOrNull(ESPERA_MAXIMA) { Result.success(reservasRepository.observarReserva(reservaId).first()) }
        } catch (e: ErrorReservas) {
            Result.failure(e)
        } ?: return true
        if (lectura.isFailure) return true
        val reserva = lectura.getOrNull()
        val sigue = reserva != null && reserva.vigente && reserva.fecha == fecha && reserva.hora == hora
        if (!sigue) recordatoriosRepository.cancelar(reservaId)
        return sigue
    }

    private companion object {
        const val ESPERA_MAXIMA = 10_000L
    }
}
