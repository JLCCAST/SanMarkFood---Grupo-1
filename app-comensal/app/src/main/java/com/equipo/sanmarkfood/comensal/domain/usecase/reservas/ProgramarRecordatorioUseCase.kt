package com.equipo.sanmarkfood.comensal.domain.usecase.reservas

import com.equipo.sanmarkfood.comensal.domain.model.reservas.AnticipacionRecordatorio
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Recordatorio
import com.equipo.sanmarkfood.comensal.domain.repository.RecordatoriosRepository
import javax.inject.Inject

class ProgramarRecordatorioUseCase @Inject constructor(
    private val recordatoriosRepository: RecordatoriosRepository,
) {
    operator fun invoke(
        reservaId: String,
        restauranteNombre: String,
        fecha: String,
        hora: String,
        anticipacion: AnticipacionRecordatorio?,
        ahora: Long = System.currentTimeMillis(),
    ): Boolean {
        if (anticipacion == null) {
            recordatoriosRepository.cancelar(reservaId)
            return false
        }
        val recordatorio = Recordatorio(reservaId, restauranteNombre, fecha, hora, anticipacion)
        val aviso = recordatorio.momentoDelAviso
        if (aviso == null || aviso <= ahora) {
            recordatoriosRepository.cancelar(reservaId)
            return false
        }
        recordatoriosRepository.programar(recordatorio, aviso - ahora)
        return true
    }
}
