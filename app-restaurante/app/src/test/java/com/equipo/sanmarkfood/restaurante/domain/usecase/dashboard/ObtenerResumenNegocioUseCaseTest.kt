package com.equipo.sanmarkfood.restaurante.domain.usecase.dashboard

import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.CifrasVentas
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.PeriodoNegocio
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.PlatoPedido
import com.equipo.sanmarkfood.restaurante.domain.repository.DashboardRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ObtenerResumenNegocioUseCaseTest {

    private class RepositorioFalso(
        private val porTramo: CifrasVentas = CifrasVentas(pedidos = 2, centimos = 3_000),
        private val anterior: CifrasVentas = CifrasVentas(pedidos = 4, centimos = 8_000),
    ) : DashboardRepository {
        val rangosVentas: MutableList<Pair<Date, Date>> = Collections.synchronizedList(mutableListOf())
        val rangosReservas: MutableList<Pair<Date, Date>> = Collections.synchronizedList(mutableListOf())
        var rangoDelRanking: Pair<Date, Date>? = null
        var inicioDelPeriodoAnterior: Date? = null

        override suspend fun ventasEntre(desde: Date, hasta: Date): CifrasVentas {
            rangosVentas.add(desde to hasta)
            return if (desde == inicioDelPeriodoAnterior) anterior else porTramo
        }

        override suspend fun reservasConfirmadasEntre(desde: Date, hasta: Date): Int {
            rangosReservas.add(desde to hasta)
            return if (desde == inicioDelPeriodoAnterior) 5 else 3
        }

        override suspend fun promedioDeResenasHasta(hasta: Date): Double? = null

        override suspend fun platosMasPedidos(desde: Date, hasta: Date): List<PlatoPedido> {
            rangoDelRanking = desde to hasta
            return listOf(PlatoPedido("Menú del día", 12), PlatoPedido("Lomo saltado", 4))
        }
    }

    private val formato = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).apply {
        timeZone = TimeZone.getTimeZone("America/Lima")
    }

    private fun lima(texto: String): Date = formato.parse(texto)!!

    private val miercoles = lima("2026-11-18 15:00")

    @Test
    fun `la semana va de lunes a domingo y solo consulta los dias que ya empezaron`() {
        val repositorio = RepositorioFalso().apply { inicioDelPeriodoAnterior = lima("2026-11-09 00:00") }

        val resumen = runBlocking { ObtenerResumenNegocioUseCase(repositorio)(PeriodoNegocio.SEMANA, miercoles) }

        assertEquals(lima("2026-11-16 00:00"), resumen.tramos.first().inicio)
        assertEquals(lima("2026-11-22 00:00"), resumen.tramos.last().inicio)
        assertEquals(listOf(2, 2, 2, 0, 0, 0, 0), resumen.tramos.map { it.pedidos })
        assertEquals(2, resumen.tramoActual)
        assertTrue(lima("2026-11-18 00:00") to miercoles in repositorio.rangosVentas)
        assertEquals(CifrasVentas(pedidos = 6, centimos = 9_000), resumen.actual)
    }

    @Test
    fun `los platos mas pedidos son los del mismo periodo`() {
        val repositorio = RepositorioFalso().apply { inicioDelPeriodoAnterior = lima("2026-09-28 00:00") }

        val resumen = runBlocking { ObtenerResumenNegocioUseCase(repositorio)(PeriodoNegocio.MES, miercoles) }

        assertEquals(lima("2026-10-26 00:00") to miercoles, repositorio.rangoDelRanking)
        assertEquals("Menú del día", resumen.platosMasPedidos.first().nombre)
    }

    @Test
    fun `la semana se compara con el mismo tramo de la semana anterior`() {
        val repositorio = RepositorioFalso().apply { inicioDelPeriodoAnterior = lima("2026-11-09 00:00") }

        val resumen = runBlocking { ObtenerResumenNegocioUseCase(repositorio)(PeriodoNegocio.SEMANA, miercoles) }

        assertTrue(lima("2026-11-09 00:00") to lima("2026-11-11 15:00") in repositorio.rangosVentas)
        assertTrue(lima("2026-11-16 00:00") to miercoles in repositorio.rangosReservas)
        assertEquals(2, resumen.diferenciaPedidos)
        assertEquals(13, resumen.variacionVentas)
        assertEquals(-2, resumen.diferenciaReservas)
    }

    @Test
    fun `el mes son las ultimas cuatro semanas, la actual en curso`() {
        val repositorio = RepositorioFalso().apply { inicioDelPeriodoAnterior = lima("2026-09-28 00:00") }

        val resumen = runBlocking { ObtenerResumenNegocioUseCase(repositorio)(PeriodoNegocio.MES, miercoles) }

        assertEquals(
            listOf("2026-10-26 00:00", "2026-11-02 00:00", "2026-11-09 00:00", "2026-11-16 00:00").map(::lima),
            resumen.tramos.map { it.inicio },
        )
        assertEquals(3, resumen.tramoActual)
        assertTrue(lima("2026-11-16 00:00") to miercoles in repositorio.rangosVentas)
        assertTrue(lima("2026-09-28 00:00") to lima("2026-10-21 15:00") in repositorio.rangosVentas)
        assertEquals(8, resumen.actual.pedidos)
    }

    @Test
    fun `sin ventas en el periodo anterior no hay porcentaje`() {
        val repositorio = RepositorioFalso(anterior = CifrasVentas.Cero).apply {
            inicioDelPeriodoAnterior = lima("2026-11-09 00:00")
        }

        val resumen = runBlocking { ObtenerResumenNegocioUseCase(repositorio)(PeriodoNegocio.SEMANA, miercoles) }

        assertNull(resumen.variacionVentas)
    }
}
