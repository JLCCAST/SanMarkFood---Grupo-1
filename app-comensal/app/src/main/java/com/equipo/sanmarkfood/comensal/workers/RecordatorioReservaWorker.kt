package com.equipo.sanmarkfood.comensal.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.avisos.mostrarAviso
import com.equipo.sanmarkfood.comensal.domain.usecase.reservas.VerificarRecordatorioUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class RecordatorioReservaWorker(
    context: Context,
    parametros: WorkerParameters,
) : CoroutineWorker(context, parametros) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencias {
        fun verificarRecordatorio(): VerificarRecordatorioUseCase
    }

    override suspend fun doWork(): Result {
        val reservaId = inputData.getString(CLAVE_RESERVA) ?: return Result.failure()
        val restaurante = inputData.getString(CLAVE_RESTAURANTE).orEmpty()
        val fecha = inputData.getString(CLAVE_FECHA).orEmpty()
        val hora = inputData.getString(CLAVE_HORA).orEmpty()
        val verificar = EntryPointAccessors.fromApplication(applicationContext, Dependencias::class.java)
            .verificarRecordatorio()
        if (!verificar(reservaId, fecha, hora)) return Result.success()
        mostrarAviso(
            context = applicationContext,
            canal = applicationContext.getString(R.string.canal_reservas_id),
            id = reservaId.hashCode(),
            titulo = applicationContext.getString(R.string.recordatorio_titulo),
            texto = applicationContext.getString(R.string.recordatorio_texto, restaurante, hora),
        )
        return Result.success()
    }

    companion object {
        const val CLAVE_RESERVA = "reservaId"
        const val CLAVE_RESTAURANTE = "restauranteNombre"
        const val CLAVE_FECHA = "fecha"
        const val CLAVE_HORA = "hora"

        fun nombreDelTrabajo(reservaId: String): String = "recordatorio-$reservaId"

        fun datos(reservaId: String, restauranteNombre: String, fecha: String, hora: String) = workDataOf(
            CLAVE_RESERVA to reservaId,
            CLAVE_RESTAURANTE to restauranteNombre,
            CLAVE_FECHA to fecha,
            CLAVE_HORA to hora,
        )
    }
}
