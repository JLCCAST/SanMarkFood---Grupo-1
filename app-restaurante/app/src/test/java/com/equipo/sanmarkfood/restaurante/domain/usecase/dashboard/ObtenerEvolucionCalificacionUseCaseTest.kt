package com.equipo.sanmarkfood.restaurante.domain.usecase.dashboard

import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.CifrasVentas
import com.equipo.sanmarkfood.restaurante.domain.repository.DashboardRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class ObtenerEvolucionCalificacionUseCaseTest {

    private class RepositorioFalso(private val promedios: Map<Long, Double>) : DashboardRepository {
        override suspend fun ventasEntre(desde: Date, hasta: Date): CifrasVentas = CifrasVentas.Cero

        override suspend fun reservasConfirmadasEntre(desde: Date, hasta: Date): Int = 0

        override suspend fun promedioDeResenasHasta(hasta: Date): Double? = promedios[hasta.time]
    }

    private val ahora = Date(100 * UNA_SEMANA)

    private fun haceSemanas(semanas: Int): Long = ahora.time - semanas * UNA_SEMANA

    @Test
    fun `toma el promedio acumulado de las ultimas seis semanas y hoy`() {
        val promedios = (6 downTo 0).associate { haceSemanas(it) to 4.5 + (6 - it) * 0.05 }

        val evolucion = runBlocking { ObtenerEvolucionCalificacionUseCase(RepositorioFalso(promedios))(ahora) }

        assertEquals(7, evolucion.puntos.size)
        assertEquals(6, evolucion.semanas)
        assertEquals(4.8, evolucion.actual!!, 0.001)
        assertEquals(0.3, evolucion.cambio!!, 0.001)
    }

    @Test
    fun `un local nuevo empieza desde su primera resena`() {
        val promedios = mapOf(haceSemanas(2) to 5.0, haceSemanas(1) to 4.0, haceSemanas(0) to 4.5)

        val evolucion = runBlocking { ObtenerEvolucionCalificacionUseCase(RepositorioFalso(promedios))(ahora) }

        assertEquals(3, evolucion.puntos.size)
        assertEquals(2, evolucion.semanas)
        assertEquals(-0.5, evolucion.cambio!!, 0.001)
    }

    @Test
    fun `sin resenas no hay calificacion ni cambio`() {
        val evolucion = runBlocking { ObtenerEvolucionCalificacionUseCase(RepositorioFalso(emptyMap()))(ahora) }

        assertEquals(0, evolucion.puntos.size)
        assertNull(evolucion.actual)
        assertNull(evolucion.cambio)
    }

    private companion object {
        const val UNA_SEMANA = 7L * 24 * 60 * 60 * 1000
    }
}
