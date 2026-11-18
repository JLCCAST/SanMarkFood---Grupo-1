package com.equipo.sanmarkfood.comensal.domain.repository

import com.equipo.sanmarkfood.comensal.domain.model.reservas.AnticipacionRecordatorio
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Recordatorio
import kotlinx.coroutines.flow.Flow

interface RecordatoriosRepository {
    fun programar(recordatorio: Recordatorio, demoraMilis: Long)

    fun cancelar(reservaId: String)

    fun observarRecordatorios(): Flow<Map<String, AnticipacionRecordatorio>>
}
