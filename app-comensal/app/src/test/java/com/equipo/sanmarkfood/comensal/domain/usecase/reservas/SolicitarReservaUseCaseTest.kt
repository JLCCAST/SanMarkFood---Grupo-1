package com.equipo.sanmarkfood.comensal.domain.usecase.reservas

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.CategoriaRestaurante
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.HorarioDia
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import com.equipo.sanmarkfood.comensal.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.comensal.domain.model.reservas.EstadoReserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.SolicitudReserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.momentoDeReserva
import com.equipo.sanmarkfood.comensal.domain.repository.ReservasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SolicitarReservaUseCaseTest {

    private class ReservasFalsas : ReservasRepository {
        val creadas = mutableListOf<SolicitudReserva>()

        override fun observarReservas(comensalUid: String): Flow<List<Reserva>> = emptyFlow()
        override fun observarReserva(reservaId: String): Flow<Reserva?> = emptyFlow()
        override fun observarOcupacion(restauranteId: String, fecha: String): Flow<Map<String, Int>> = emptyFlow()
        override suspend fun crearReserva(solicitud: SolicitudReserva): String {
            creadas += solicitud
            return "reserva-1"
        }
        override suspend fun modificarReserva(reservaId: String, fecha: String, hora: String) = Unit
        override suspend fun cancelarReserva(reservaId: String) = Unit
    }

    private val ahora = momentoDeReserva("2026-09-17", "10:10")!!

    private fun local(aceptaReservas: Boolean = true) = Restaurante(
        id = "local",
        nombre = "Verde Campus",
        direccion = "",
        categoria = CategoriaRestaurante.VEGETARIANA,
        ubicacion = null,
        portadaUrl = "",
        logoUrl = "",
        pausado = false,
        calificacionPromedio = null,
        totalResenas = 0,
        horario = listOf("lun", "mar", "mie", "jue", "vie", "sab", "dom")
            .associateWith { HorarioDia(true, "12:00", "16:00") },
        aceptaReservas = aceptaReservas,
    )

    private fun activa(fecha: String) = Reserva(
        id = fecha, restauranteId = "otro", restauranteNombre = "Otro", fecha = fecha, hora = "13:00",
        personas = 2, estado = EstadoReserva.PENDIENTE, rechazo = null, horarioAnterior = null,
        creadoEn = null, respondidaEn = null, actualizadoEn = null,
    )

    private fun esperarError(esperado: ErrorReservas, bloque: suspend () -> Unit) = runBlocking {
        try {
            bloque()
            fail("Se esperaba $esperado")
        } catch (e: ErrorReservas) {
            assertEquals(esperado, e)
        }
    }

    @Test
    fun `envia la solicitud valida a la funcion reservas`() = runBlocking {
        val repositorio = ReservasFalsas()

        val id = SolicitarReservaUseCase(repositorio)(local(), "2026-09-18", "13:30", 3, emptyList(), ahora)

        assertEquals("reserva-1", id)
        assertEquals(listOf(SolicitudReserva("local", "2026-09-18", "13:30", 3)), repositorio.creadas)
    }

    @Test
    fun `no envia nada si el local no acepta reservas`() {
        val repositorio = ReservasFalsas()

        esperarError(ErrorReservas.SinReservas) {
            SolicitarReservaUseCase(repositorio)(local(aceptaReservas = false), "2026-09-18", "13:30", 3, emptyList(), ahora)
        }
        assertTrue(repositorio.creadas.isEmpty())
    }

    @Test
    fun `no envia nada si ya tiene dos reservas activas`() {
        val repositorio = ReservasFalsas()
        val reservas = listOf(activa("2026-09-18"), activa("2026-09-19"))

        esperarError(ErrorReservas.TopeReservas) {
            SolicitarReservaUseCase(repositorio)(local(), "2026-09-20", "13:30", 2, reservas, ahora)
        }
        assertTrue(repositorio.creadas.isEmpty())
    }
}
