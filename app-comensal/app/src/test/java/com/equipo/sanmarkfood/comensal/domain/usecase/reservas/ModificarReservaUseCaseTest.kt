package com.equipo.sanmarkfood.comensal.domain.usecase.reservas

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.HorarioDia
import com.equipo.sanmarkfood.comensal.domain.model.reservas.AnticipacionRecordatorio
import com.equipo.sanmarkfood.comensal.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.comensal.domain.model.reservas.EstadoReserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Recordatorio
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.SolicitudReserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.momentoDeReserva
import com.equipo.sanmarkfood.comensal.domain.repository.RecordatoriosRepository
import com.equipo.sanmarkfood.comensal.domain.repository.ReservasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ModificarReservaUseCaseTest {

    private class ReservasFalsas : ReservasRepository {
        val cambios = mutableListOf<Triple<String, String, String>>()

        override fun observarReservas(comensalUid: String): Flow<List<Reserva>> = emptyFlow()
        override fun observarReserva(reservaId: String): Flow<Reserva?> = emptyFlow()
        override fun observarOcupacion(restauranteId: String, fecha: String): Flow<Map<String, Int>> = emptyFlow()
        override suspend fun crearReserva(solicitud: SolicitudReserva): String = "nueva"
        override suspend fun modificarReserva(reservaId: String, fecha: String, hora: String) {
            cambios += Triple(reservaId, fecha, hora)
        }
        override suspend fun cancelarReserva(reservaId: String) = Unit
    }

    private class RecordatoriosFalsos : RecordatoriosRepository {
        val programados = mutableListOf<Recordatorio>()

        override fun programar(recordatorio: Recordatorio, demoraMilis: Long) {
            programados += recordatorio
        }
        override fun cancelar(reservaId: String) = Unit
        override fun observarRecordatorios(): Flow<Map<String, AnticipacionRecordatorio>> = emptyFlow()
    }

    private val ahora = momentoDeReserva("2026-09-17", "10:00")!!
    private val horario = listOf("lun", "mar", "mie", "jue", "vie", "sab", "dom")
        .associateWith { HorarioDia(true, "12:00", "16:00") }

    private val reserva = Reserva(
        id = "r1", restauranteId = "local", restauranteNombre = "Verde Campus", fecha = "2026-09-18", hora = "13:00",
        personas = 2, estado = EstadoReserva.CONFIRMADA, rechazo = null, horarioAnterior = null,
        creadoEn = null, respondidaEn = null, actualizadoEn = null,
    )

    @Test
    fun `cambia el horario y mueve el recordatorio a la nueva hora`() = runBlocking {
        val reservas = ReservasFalsas()
        val recordatorios = RecordatoriosFalsos()
        val caso = ModificarReservaUseCase(reservas, ProgramarRecordatorioUseCase(recordatorios))

        val recordatorio = caso(reserva, "2026-09-19", "14:30", horario, AnticipacionRecordatorio.UNA_HORA, ahora)

        assertEquals(AnticipacionRecordatorio.UNA_HORA, recordatorio)
        assertEquals(listOf(Triple("r1", "2026-09-19", "14:30")), reservas.cambios)
        assertEquals("14:30", recordatorios.programados.single().hora)
    }

    @Test
    fun `no envia el cambio si el horario es el mismo`() = runBlocking {
        val reservas = ReservasFalsas()
        val caso = ModificarReservaUseCase(reservas, ProgramarRecordatorioUseCase(RecordatoriosFalsos()))
        try {
            caso(reserva, "2026-09-18", "13:00", horario, null, ahora)
            fail("Se esperaba SinCambios")
        } catch (e: ErrorReservas) {
            assertEquals(ErrorReservas.SinCambios, e)
        }
        assertTrue(reservas.cambios.isEmpty())
    }
}
