package com.equipo.sanmarkfood.comensal.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.equipo.sanmarkfood.comensal.domain.model.reservas.AnticipacionRecordatorio
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Recordatorio
import com.equipo.sanmarkfood.comensal.domain.repository.RecordatoriosRepository
import com.equipo.sanmarkfood.comensal.workers.RecordatorioReservaWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RecordatoriosRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : RecordatoriosRepository {

    private val preferencias: SharedPreferences =
        context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)

    override fun programar(recordatorio: Recordatorio, demoraMilis: Long) {
        val trabajo = OneTimeWorkRequestBuilder<RecordatorioReservaWorker>()
            .setInitialDelay(demoraMilis, TimeUnit.MILLISECONDS)
            .setInputData(
                RecordatorioReservaWorker.datos(
                    recordatorio.reservaId,
                    recordatorio.restauranteNombre,
                    recordatorio.fecha,
                    recordatorio.hora,
                )
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            RecordatorioReservaWorker.nombreDelTrabajo(recordatorio.reservaId),
            ExistingWorkPolicy.REPLACE,
            trabajo,
        )
        preferencias.edit { putInt(recordatorio.reservaId, recordatorio.anticipacion.minutos) }
    }

    override fun cancelar(reservaId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(RecordatorioReservaWorker.nombreDelTrabajo(reservaId))
        preferencias.edit { remove(reservaId) }
    }

    override fun observarRecordatorios(): Flow<Map<String, AnticipacionRecordatorio>> = callbackFlow {
        val escucha = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(leer()) }
        preferencias.registerOnSharedPreferenceChangeListener(escucha)
        trySend(leer())
        awaitClose { preferencias.unregisterOnSharedPreferenceChangeListener(escucha) }
    }

    private fun leer(): Map<String, AnticipacionRecordatorio> =
        preferencias.all.mapNotNull { (reservaId, minutos) ->
            (minutos as? Int)?.let(AnticipacionRecordatorio::desde)?.let { reservaId to it }
        }.toMap()

    private companion object {
        const val PREFERENCIAS = "recordatorios_de_reservas"
    }
}
