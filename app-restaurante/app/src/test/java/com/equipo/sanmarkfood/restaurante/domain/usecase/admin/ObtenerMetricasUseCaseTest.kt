package com.equipo.sanmarkfood.restaurante.domain.usecase.admin

import com.equipo.sanmarkfood.restaurante.domain.model.admin.LocalAprobado
import com.equipo.sanmarkfood.restaurante.domain.model.admin.LocalConPedidos
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.repository.MetricasRepository
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

class ObtenerMetricasUseCaseTest {

    private class RepositorioFalso(
        private val pedidosEstaSemana: Int = 21,
        private val pedidosSemanaAnterior: Int = 20,
        private val pedidosPorLocal: Map<String, Int> = emptyMap(),
    ) : MetricasRepository {
        val rangosPedidos: MutableList<Pair<Date, Date>> = Collections.synchronizedList(mutableListOf())
        val rangosDeLocal: MutableList<Pair<Date, Date>> = Collections.synchronizedList(mutableListOf())
        var inicioEstaSemana: Date? = null

        override suspend fun contarRestaurantes(estado: EstadoRestaurante): Int =
            if (estado == EstadoRestaurante.APROBADO) 24 else 3

        override suspend fun contarReenviados(): Int = 1

        override suspend fun contarResenasReportadas(): Int = 0

        override suspend fun contarPedidos(desde: Date, hasta: Date): Int {
            rangosPedidos.add(desde to hasta)
            return if (desde == inicioEstaSemana) pedidosEstaSemana else pedidosSemanaAnterior
        }

        override suspend fun listarAprobados(): List<LocalAprobado> =
            pedidosPorLocal.keys.map { LocalAprobado(uid = it, nombre = "Local $it") }

        override suspend fun contarPedidosDeLocal(uid: String, desde: Date, hasta: Date): Int {
            rangosDeLocal.add(desde to hasta)
            return pedidosPorLocal.getValue(uid)
        }
    }

    private val formato = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).apply {
        timeZone = TimeZone.getTimeZone("America/Lima")
    }

    private fun lima(texto: String): Date = formato.parse(texto)!!

    private fun rangosDe(ahora: Date, inicioEstaSemana: Date): Set<Pair<Date, Date>> {
        val repositorio = RepositorioFalso().apply { this.inicioEstaSemana = inicioEstaSemana }
        runBlocking { ObtenerMetricasUseCase(repositorio)(ahora) }
        return repositorio.rangosPedidos.toSet()
    }

    @Test
    fun juntaLasCifrasDeLaPlataforma() = runBlocking {
        val repositorio = RepositorioFalso().apply { inicioEstaSemana = lima("2026-10-26 00:00") }

        val metricas = ObtenerMetricasUseCase(repositorio)(lima("2026-10-28 12:00"))

        assertEquals(24, metricas.restaurantesActivos)
        assertEquals(3, metricas.pendientes)
        assertEquals(1, metricas.reenviados)
        assertEquals(0, metricas.resenasReportadas)
        assertEquals(21, metricas.pedidosEstaSemana)
        assertEquals(20, metricas.pedidosSemanaAnterior)
        assertEquals(5, metricas.variacionPedidos)
    }

    @Test
    fun comparaConElMismoTramoDeLaSemanaAnterior() {
        val ahora = lima("2026-10-28 12:00")
        val esperados = setOf(
            lima("2026-10-26 00:00") to ahora,
            lima("2026-10-19 00:00") to lima("2026-10-21 12:00"),
        )
        assertTrue(rangosDe(ahora, lima("2026-10-26 00:00")).containsAll(esperados))
    }

    @Test
    fun laSemanaEmpiezaElLunesAunqueSeaDomingo() {
        val ahora = lima("2026-11-01 23:00")
        val esperados = setOf(
            lima("2026-10-26 00:00") to ahora,
            lima("2026-10-19 00:00") to lima("2026-10-25 23:00"),
        )
        assertTrue(rangosDe(ahora, lima("2026-10-26 00:00")).containsAll(esperados))
    }

    @Test
    fun cruzaElCambioDeAnio() {
        val ahora = lima("2027-01-01 10:00")
        val esperados = setOf(
            lima("2026-12-28 00:00") to ahora,
            lima("2026-12-21 00:00") to lima("2026-12-25 10:00"),
        )
        assertTrue(rangosDe(ahora, lima("2026-12-28 00:00")).containsAll(esperados))
    }

    @Test
    fun graficaLasOchoSemanasDeLaMasAntiguaALaActual() = runBlocking {
        val ahora = lima("2026-10-28 12:00")
        val repositorio = RepositorioFalso().apply { inicioEstaSemana = lima("2026-10-26 00:00") }

        val semanas = ObtenerMetricasUseCase(repositorio)(ahora).pedidosPorSemana

        assertEquals(8, semanas.size)
        assertEquals(lima("2026-09-07 00:00"), semanas.first().inicio)
        assertEquals(lima("2026-10-26 00:00"), semanas.last().inicio)
        assertEquals(21, semanas.last().pedidos)
        assertTrue(repositorio.rangosPedidos.contains(lima("2026-10-19 00:00") to lima("2026-10-26 00:00")))
        assertTrue(repositorio.rangosPedidos.contains(lima("2026-10-26 00:00") to ahora))
    }

    @Test
    fun ordenaLosCuatroLocalesConMasPedidosDeLaSemana() = runBlocking {
        val ahora = lima("2026-10-28 12:00")
        val repositorio = RepositorioFalso(
            pedidosPorLocal = mapOf("a" to 3, "b" to 0, "c" to 9, "d" to 1, "e" to 5, "f" to 2),
        )

        val locales = ObtenerMetricasUseCase(repositorio)(ahora).localesConMasPedidos

        assertEquals(
            listOf(
                LocalConPedidos("Local c", 9),
                LocalConPedidos("Local e", 5),
                LocalConPedidos("Local a", 3),
                LocalConPedidos("Local f", 2),
            ),
            locales,
        )
        assertEquals(setOf(lima("2026-10-26 00:00") to ahora), repositorio.rangosDeLocal.toSet())
    }

    @Test
    fun noListaLocalesSinPedidos() = runBlocking {
        val repositorio = RepositorioFalso(pedidosPorLocal = mapOf("a" to 0, "b" to 0))

        val locales = ObtenerMetricasUseCase(repositorio)(lima("2026-10-28 12:00")).localesConMasPedidos

        assertEquals(emptyList<LocalConPedidos>(), locales)
    }

    @Test
    fun noHayVariacionSiLaSemanaAnteriorNoTuvoPedidos() = runBlocking {
        val repositorio = RepositorioFalso(pedidosSemanaAnterior = 0).apply {
            inicioEstaSemana = lima("2026-10-26 00:00")
        }

        val metricas = ObtenerMetricasUseCase(repositorio)(lima("2026-10-28 12:00"))

        assertNull(metricas.variacionPedidos)
    }
}
