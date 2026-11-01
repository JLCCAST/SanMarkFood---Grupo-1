package com.equipo.sanmarkfood.restaurante.domain.usecase.admin

import com.equipo.sanmarkfood.restaurante.domain.model.admin.MetricasPlataforma
import com.equipo.sanmarkfood.restaurante.domain.model.admin.PedidosSemana
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.menu.inicioDeSemana
import com.equipo.sanmarkfood.restaurante.domain.repository.MetricasRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.Date
import javax.inject.Inject

class ObtenerMetricasUseCase @Inject constructor(
    private val metricasRepository: MetricasRepository
) {
    suspend operator fun invoke(ahora: Date = Date()): MetricasPlataforma = coroutineScope {
        val activos = async { metricasRepository.contarRestaurantes(EstadoRestaurante.APROBADO) }
        val pendientes = async { metricasRepository.contarRestaurantes(EstadoRestaurante.PENDIENTE) }
        val reenviados = async { metricasRepository.contarReenviados() }
        val reportadas = async { metricasRepository.contarResenasReportadas() }
        val semanaAnterior = async {
            metricasRepository.contarPedidos(inicioDeSemana(ahora, semanasAtras = 1), Date(ahora.time - UNA_SEMANA))
        }
        val semanas = (SEMANAS_DEL_GRAFICO - 1 downTo 0).map { atras ->
            async {
                val inicio = inicioDeSemana(ahora, atras)
                val fin = if (atras == 0) ahora else inicioDeSemana(ahora, atras - 1)
                PedidosSemana(inicio, metricasRepository.contarPedidos(inicio, fin))
            }
        }.awaitAll()

        MetricasPlataforma(
            restaurantesActivos = activos.await(),
            pendientes = pendientes.await(),
            reenviados = reenviados.await(),
            resenasReportadas = reportadas.await(),
            pedidosEstaSemana = semanas.last().pedidos,
            pedidosSemanaAnterior = semanaAnterior.await(),
            pedidosPorSemana = semanas,
        )
    }

    private companion object {
        const val UNA_SEMANA = 7L * 24 * 60 * 60 * 1000
        const val SEMANAS_DEL_GRAFICO = 8
    }
}
