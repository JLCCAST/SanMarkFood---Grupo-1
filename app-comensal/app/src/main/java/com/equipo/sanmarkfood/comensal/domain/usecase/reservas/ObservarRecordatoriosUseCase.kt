package com.equipo.sanmarkfood.comensal.domain.usecase.reservas

import com.equipo.sanmarkfood.comensal.domain.model.reservas.AnticipacionRecordatorio
import com.equipo.sanmarkfood.comensal.domain.repository.RecordatoriosRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservarRecordatoriosUseCase @Inject constructor(
    private val recordatoriosRepository: RecordatoriosRepository,
) {
    operator fun invoke(): Flow<Map<String, AnticipacionRecordatorio>> = recordatoriosRepository.observarRecordatorios()
}
