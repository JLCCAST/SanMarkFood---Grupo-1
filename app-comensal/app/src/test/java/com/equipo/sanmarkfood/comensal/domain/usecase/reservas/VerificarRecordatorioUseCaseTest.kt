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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VerificarRecordatorioUseCaseTest {

    private class ReservasFalsas(private val lectura: Flow<Reserva?>) : ReservasRepository {
        override fun observarReservas(comensalUid: String): Flow<List<Reserva>> = emptyFlow()
        override fun observarReserva(reservaId: String): Flow<Reserva?> = lectura
        override fun observarOcupacion(restauranteId: String, fecha: String): Flow<Map<String, Int>> = emptyFlow()
        override suspend fun crearReserva(solicitud: SolicitudReserva): String = "nueva"
        override suspend fun modificarReserva(reservaId: String, fecha: String, hora: String) = Unit
        override suspend fun cancelarReserva(reservaId: String) = Unit
    }

    private class RecordatoriosFalsos : RecordatoriosRepository {
        val cancelados = mutableListOf<String>()

        override fun programar(recordatorio: Recordatorio, demoraMilis: Long) = Unit
        override fun cancelar(reservaId: String) {
            cancelados += reservaId
        }
        override fun observarRecordatorios(): Flow<Map<String, AnticipacionRecordatorio>> = emptyFlow()
    }

    private fun reserva(estado: EstadoReserva, hora: String = "13:00") = Reserva(
        id = "r1", restauranteId = "local", restauranteNombre = "Local", fecha = "2026-09-18", hora = hora,
        personas = 2, estado = estado, rechazo = null, horarioAnterior = null,
        creadoEn = null, respondidaEn = null, actualizadoEn = null,
    )

    private fun verificar(lectura: Flow<Reserva?>, recordatorios: RecordatoriosFalsos = RecordatoriosFalsos()) =
        runBlocking { VerificarRecordatorioUseCase(ReservasFalsas(lectura), recordatorios)("r1", "2026-09-18", "13:00") }

    @Test
    fun `avisa si la reserva sigue confirmada a esa hora`() {
        assertTrue(verificar(flowOf(reserva(EstadoReserva.CONFIRMADA))))
    }

    @Test
    fun `no avisa de una reserva cancelada o rechazada`() {
        val recordatorios = RecordatoriosFalsos()

        assertFalse(verificar(flowOf(reserva(EstadoReserva.CANCELADA)), recordatorios))
        assertFalse(verificar(flowOf(reserva(EstadoReserva.RECHAZADA)), recordatorios))
        assertEquals(listOf("r1", "r1"), recordatorios.cancelados)
    }

    @Test
    fun `no avisa con un horario que ya se cambio`() {
        assertFalse(verificar(flowOf(reserva(EstadoReserva.PENDIENTE, hora = "14:30"))))
    }

    @Test
    fun `sin conexion prefiere avisar`() {
        assertTrue(verificar(flow { throw ErrorReservas.SinConexion }))
    }
}
