package com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante

import org.junit.Assert.assertEquals
import org.junit.Test

class HorarioTest {

    private fun abiertos(vararg dias: DiaSemana) = Horario(
        DiaSemana.entries.associateWith { dia ->
            HorarioDia(abierto = dia in dias, abre = Hora(11, 30), cierra = Hora(16, 0))
        }
    )

    @Test
    fun elHorarioPorDefectoEsUnSoloTramoDeLunesASabado() {
        assertEquals(listOf(DiaSemana.LUNES to DiaSemana.SABADO), Horario.PorDefecto.tramosAbiertos())
    }

    @Test
    fun separaLosTramosCuandoHayDiasCerradosEnMedio() {
        val horario = abiertos(
            DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES,
            DiaSemana.DOMINGO,
        )
        assertEquals(
            listOf(DiaSemana.LUNES to DiaSemana.VIERNES, DiaSemana.DOMINGO to DiaSemana.DOMINGO),
            horario.tramosAbiertos(),
        )
    }

    @Test
    fun cubreLosCasosDeLosExtremos() {
        assertEquals(listOf(DiaSemana.LUNES to DiaSemana.DOMINGO), abiertos(*DiaSemana.entries.toTypedArray()).tramosAbiertos())
        assertEquals(emptyList<Pair<DiaSemana, DiaSemana>>(), abiertos().tramosAbiertos())
        assertEquals(
            listOf(DiaSemana.MARTES to DiaSemana.MARTES, DiaSemana.JUEVES to DiaSemana.JUEVES),
            abiertos(DiaSemana.MARTES, DiaSemana.JUEVES).tramosAbiertos(),
        )
    }
}
