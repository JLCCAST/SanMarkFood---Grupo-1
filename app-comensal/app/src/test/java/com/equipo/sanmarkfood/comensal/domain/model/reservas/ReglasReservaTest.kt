package com.equipo.sanmarkfood.comensal.domain.model.reservas

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.HorarioDia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasReservaTest {

    private val ahora = momentoDeReserva("2026-09-17", "10:10")!!

    private val horario = mapOf(
        "lun" to HorarioDia(true, "12:00", "16:00"),
        "mar" to HorarioDia(true, "12:00", "16:00"),
        "mie" to HorarioDia(true, "12:00", "16:00"),
        "jue" to HorarioDia(true, "11:45", "16:00"),
        "vie" to HorarioDia(true, "12:00", "16:00"),
        "sab" to HorarioDia(true, "12:00", "16:00"),
        "dom" to HorarioDia(false, "12:00", "16:00"),
    )

    private fun solicitud(fecha: String = "2026-09-18", hora: String = "13:00", personas: Int = 2) =
        SolicitudReserva("local", fecha, hora, personas)

    private fun reserva(
        fecha: String,
        estado: EstadoReserva = EstadoReserva.PENDIENTE,
        hora: String = "13:00",
    ) = Reserva(
        id = fecha + hora,
        restauranteId = "local",
        restauranteNombre = "Local",
        fecha = fecha,
        hora = hora,
        personas = 2,
        estado = estado,
        rechazo = null,
        horarioAnterior = null,
        creadoEn = null,
        respondidaEn = null,
        actualizadoEn = null,
    )

    @Test
    fun `ofrece siete dias desde hoy y marca el dia cerrado`() {
        val dias = diasReservables(horario, ahora)

        assertEquals(7, dias.size)
        assertEquals("2026-09-17", dias.first().fecha)
        assertTrue(dias.first().esHoy)
        assertEquals("2026-09-23", dias.last().fecha)
        val domingo = dias.first { it.claveDia == "dom" }
        assertTrue(domingo.cerrado)
        assertFalse(domingo.disponible)
    }

    @Test
    fun `hoy solo ofrece horarios con dos horas de anticipacion`() {
        val horas = horariosDelDia(horario, "2026-09-17", ahora, 2).map { it.hora }

        assertEquals("12:30", primeraHoraDeHoy(ahora))
        assertEquals(listOf("12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30"), horas)
    }

    @Test
    fun `los horarios van cada media hora dentro del horario del local`() {
        val horas = horariosDelDia(horario, "2026-09-18", ahora, 2).map { it.hora }

        assertEquals(listOf("12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30"), horas)
        assertTrue(horariosDelDia(horario, "2026-09-20", ahora, 2).isEmpty())
    }

    @Test
    fun `marca llena la franja que no alcanza para el grupo`() {
        val horas = horariosDelDia(
            horario, "2026-09-18", ahora, personas = 4,
            ocupacion = mapOf("13:00" to 9, "13:30" to 8), cupoPorFranja = 12,
        ).associate { it.hora to it.lleno }

        assertTrue(horas.getValue("13:00"))
        assertFalse(horas.getValue("13:30"))
        assertFalse(horas.getValue("14:00"))
    }

    @Test
    fun `rechaza menos de dos horas o mas de siete dias de anticipacion`() {
        assertEquals(ErrorReservas.FueraDePlazo, validarSolicitud(solicitud("2026-09-17", "12:00"), horario, ahora, emptyList()))
        assertEquals(ErrorReservas.FueraDePlazo, validarSolicitud(solicitud("2026-09-24", "13:00"), horario, ahora, emptyList()))
        assertNull(validarSolicitud(solicitud("2026-09-17", "12:30"), horario, ahora, emptyList()))
        assertNull(validarSolicitud(solicitud("2026-09-24", "10:00"), horario.plus("jue" to HorarioDia(true, "10:00", "16:00")), ahora, emptyList()))
    }

    @Test
    fun `rechaza una hora fuera del horario o que no cae en media hora`() {
        assertEquals(ErrorReservas.FueraDeHorario, validarSolicitud(solicitud(hora = "16:00"), horario, ahora, emptyList()))
        assertEquals(ErrorReservas.FueraDeHorario, validarSolicitud(solicitud(hora = "13:15"), horario, ahora, emptyList()))
        assertEquals(ErrorReservas.FueraDeHorario, validarSolicitud(solicitud("2026-09-20"), horario, ahora, emptyList()))
    }

    @Test
    fun `acepta de uno a diez comensales`() {
        assertEquals(ErrorReservas.ComensalesInvalidos, validarSolicitud(solicitud(personas = 0), horario, ahora, emptyList()))
        assertEquals(ErrorReservas.ComensalesInvalidos, validarSolicitud(solicitud(personas = 11), horario, ahora, emptyList()))
        assertNull(validarSolicitud(solicitud(personas = 1), horario, ahora, emptyList()))
        assertNull(validarSolicitud(solicitud(personas = 10), horario, ahora, emptyList()))
    }

    @Test
    fun `no deja tener mas de dos reservas activas`() {
        val dosActivas = listOf(reserva("2026-09-18"), reserva("2026-09-19", EstadoReserva.CONFIRMADA))
        val unaActivaYOtras = listOf(
            reserva("2026-09-18"),
            reserva("2026-09-19", EstadoReserva.CANCELADA),
            reserva("2026-09-19", EstadoReserva.RECHAZADA),
            reserva("2026-09-16", EstadoReserva.CONFIRMADA),
        )

        assertEquals(ErrorReservas.TopeReservas, validarSolicitud(solicitud(), horario, ahora, dosActivas))
        assertNull(validarSolicitud(solicitud(), horario, ahora, unaActivaYOtras))
    }

    @Test
    fun `el cambio exige otro horario valido y una reserva vigente`() {
        val actual = reserva("2026-09-18", EstadoReserva.CONFIRMADA)

        assertEquals(ErrorReservas.SinCambios, validarCambio(actual, "2026-09-18", "13:00", horario, ahora))
        assertEquals(ErrorReservas.FueraDeHorario, validarCambio(actual, "2026-09-20", "13:00", horario, ahora))
        assertEquals(ErrorReservas.EstadoInvalido, validarCambio(reserva("2026-09-18", EstadoReserva.CANCELADA), "2026-09-19", "13:00", horario, ahora))
        assertNull(validarCambio(actual, "2026-09-19", "14:30", horario, ahora))
    }

    @Test
    fun `el restaurante tiene hasta sesenta minutos antes para responder`() {
        val pendiente = reserva("2026-09-18", hora = "13:30")

        assertEquals("12:30", horaLimaDe(pendiente.limiteDeRespuesta!!))
        assertEquals("2026-09-18", fechaLimaDe(pendiente.limiteDeRespuesta!!))
    }
}
