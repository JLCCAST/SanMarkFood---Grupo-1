package com.equipo.sanmarkfood.comensal.domain.usecase.reservas

import com.equipo.sanmarkfood.comensal.domain.model.reservas.AnticipacionRecordatorio
import com.equipo.sanmarkfood.comensal.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.comensal.domain.model.reservas.EstadoReserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Recordatorio
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.SolicitudReserva
import com.equipo.sanmarkfood.comensal.domain.repository.RecordatoriosRepository
import com.equipo.sanmarkfood.comensal.domain.repository.ReservasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class CancelarReservaUseCaseTest {

    private class ReservasFalsas(private val falla: ErrorReservas? = null) : ReservasRepository {
        val canceladas = mutableListOf<String>()

        override fun observarReservas(comensalUid: String): Flow<List<Reserva>> = emptyFlow()
        override fun observarReserva(reservaId: String): Flow<Reserva?> = emptyFlow()
        override fun observarOcupacion(restauranteId: String, fecha: String): Flow<Map<String, Int>> = emptyFlow()
        override suspend fun crearReserva(solicitud: SolicitudReserva): String = "nueva"
        override suspend fun modificarReserva(reservaId: String, fecha: String, hora: String) = Unit
        override suspend fun cancelarReserva(reservaId: String) {
            falla?.let { throw it }
            canceladas += reservaId
        }
    }

    private class RecordatoriosFalsos : RecordatoriosRepository {
        val cancelados = mutableListOf<String>()

        override fun programar(recordatorio: Recordatorio, demoraMilis: Long) = Unit
        override fun cancelar(reservaId: String) {
            cancelados += reservaId
        }
        override fun observarRecordatorios(): Flow<Map<String, AnticipacionRecordatorio>> = emptyFlow()
    }

    private fun reserva(estado: EstadoReserva) = Reserva(
        id = "r1", restauranteId = "local", restauranteNombre = "Local", fecha = "2026-09-18", hora = "13:00",
        personas = 2, estado = estado, rechazo = null, horarioAnterior = null,
        creadoEn = null, respondidaEn = null, actualizadoEn = null,
    )

    @Test
    fun `cancela la reserva y su recordatorio`() = runBlocking {
        val reservas = ReservasFalsas()
        val recordatorios = RecordatoriosFalsos()

        CancelarReservaUseCase(reservas, recordatorios)(reserva(EstadoReserva.CONFIRMADA))

        assertEquals(listOf("r1"), reservas.canceladas)
        assertEquals(listOf("r1"), recordatorios.cancelados)
    }

    @Test
    fun `no cancela una reserva rechazada`() = runBlocking {
        val reservas = ReservasFalsas()
        try {
            CancelarReservaUseCase(reservas, RecordatoriosFalsos())(reserva(EstadoReserva.RECHAZADA))
            fail("Se esperaba EstadoInvalido")
        } catch (e: ErrorReservas) {
            assertEquals(ErrorReservas.EstadoInvalido, e)
        }
        assertTrue(reservas.canceladas.isEmpty())
    }

    @Test
    fun `si falla la funcion conserva el recordatorio`() = runBlocking {
        val recordatorios = RecordatoriosFalsos()
        try {
            CancelarReservaUseCase(ReservasFalsas(ErrorReservas.SinConexion), recordatorios)(reserva(EstadoReserva.PENDIENTE))
            fail("Se esperaba SinConexion")
        } catch (e: ErrorReservas) {
            assertEquals(ErrorReservas.SinConexion, e)
        }
        assertTrue(recordatorios.cancelados.isEmpty())
    }
}
