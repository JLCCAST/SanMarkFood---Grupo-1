package com.equipo.sanmarkfood.comensal.domain.model.reservas

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EstadoVisibleReservaTest {

    private val ahora = momentoDeReserva("2026-09-17", "10:00")!!

    private fun reserva(
        fecha: String,
        hora: String,
        estado: EstadoReserva,
        rechazo: RechazoReserva? = null,
    ) = Reserva(
        id = "$fecha $hora", restauranteId = "local", restauranteNombre = "Local", fecha = fecha, hora = hora,
        personas = 2, estado = estado, rechazo = rechazo, horarioAnterior = null,
        creadoEn = null, respondidaEn = null, actualizadoEn = null,
    )

    @Test
    fun `una reserva confirmada que ya paso se ve completada`() {
        assertEquals(
            EstadoVisibleReserva.COMPLETADA,
            reserva("2026-09-16", "13:00", EstadoReserva.CONFIRMADA).estadoVisible(ahora),
        )
        assertEquals(
            EstadoVisibleReserva.CONFIRMADA,
            reserva("2026-09-18", "13:00", EstadoReserva.CONFIRMADA).estadoVisible(ahora),
        )
    }

    @Test
    fun `el rechazo automatico se ve como sin respuesta`() {
        val automatico = RechazoReserva(MotivoRechazoReserva.SIN_RESPUESTA, null, automatico = true)
        val delLocal = RechazoReserva(MotivoRechazoReserva.SIN_MESAS, null, automatico = false)

        assertEquals(
            EstadoVisibleReserva.SIN_RESPUESTA,
            reserva("2026-09-18", "13:00", EstadoReserva.RECHAZADA, automatico).estadoVisible(ahora),
        )
        assertEquals(
            EstadoVisibleReserva.RECHAZADA,
            reserva("2026-09-18", "13:00", EstadoReserva.RECHAZADA, delLocal).estadoVisible(ahora),
        )
    }

    @Test
    fun `la proxima reserva es la vigente mas cercana que no paso`() {
        val reservas = listOf(
            reserva("2026-09-20", "13:00", EstadoReserva.PENDIENTE),
            reserva("2026-09-18", "12:30", EstadoReserva.CONFIRMADA),
            reserva("2026-09-17", "09:00", EstadoReserva.CONFIRMADA),
            reserva("2026-09-17", "13:00", EstadoReserva.CANCELADA),
        )

        assertEquals("2026-09-18 12:30", reservas.proximaReserva(ahora)?.id)
        assertNull(listOf(reservas[3]).proximaReserva(ahora))
    }
}
