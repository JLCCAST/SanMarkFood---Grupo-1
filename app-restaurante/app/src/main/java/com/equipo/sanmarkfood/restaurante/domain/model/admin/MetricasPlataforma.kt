package com.equipo.sanmarkfood.restaurante.domain.model.admin

import java.util.Date
import kotlin.math.roundToInt

data class MetricasPlataforma(
    val restaurantesActivos: Int,
    val pendientes: Int,
    val reenviados: Int,
    val resenasReportadas: Int,
    val pedidosEstaSemana: Int,
    val pedidosSemanaAnterior: Int,
    val pedidosPorSemana: List<PedidosSemana> = emptyList(),
) {
    val variacionPedidos: Int?
        get() = if (pedidosSemanaAnterior == 0) {
            null
        } else {
            ((pedidosEstaSemana - pedidosSemanaAnterior) * 100.0 / pedidosSemanaAnterior).roundToInt()
        }
}

data class PedidosSemana(val inicio: Date, val pedidos: Int)
