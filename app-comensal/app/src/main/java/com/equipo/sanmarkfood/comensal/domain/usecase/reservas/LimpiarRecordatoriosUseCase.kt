package com.equipo.sanmarkfood.comensal.domain.usecase.reservas

import com.equipo.sanmarkfood.comensal.domain.model.reservas.AnticipacionRecordatorio
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.comensal.domain.repository.RecordatoriosRepository
import javax.inject.Inject

class LimpiarRecordatoriosUseCase @Inject constructor(
    private val recordatoriosRepository: RecordatoriosRepository,
) {
    operator fun invoke(reservas: List<Reserva>, recordatorios: Map<String, AnticipacionRecordatorio>) {
        reservas
            .filter { !it.vigente && it.id in recordatorios }
            .forEach { recordatoriosRepository.cancelar(it.id) }
    }
}
