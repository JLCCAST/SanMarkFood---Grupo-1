package com.equipo.sanmarkfood.restaurante.domain.model.dashboard

import java.util.Date
import kotlin.math.roundToInt

enum class PeriodoNegocio { SEMANA, MES }

data class CifrasVentas(val pedidos: Int, val centimos: Long) {
    operator fun plus(otras: CifrasVentas) = CifrasVentas(pedidos + otras.pedidos, centimos + otras.centimos)

    companion object {
        val Cero = CifrasVentas(pedidos = 0, centimos = 0)
    }
}

data class TramoDePedidos(val inicio: Date, val pedidos: Int)

data class PlatoPedido(val nombre: String, val cantidad: Int)

data class ResumenNegocio(
    val periodo: PeriodoNegocio,
    val actual: CifrasVentas,
    val anterior: CifrasVentas,
    val reservas: Int,
    val reservasAnterior: Int,
    val tramos: List<TramoDePedidos>,
    val tramoActual: Int,
    val platosMasPedidos: List<PlatoPedido> = emptyList(),
) {
    val diferenciaPedidos: Int
        get() = actual.pedidos - anterior.pedidos

    val diferenciaReservas: Int
        get() = reservas - reservasAnterior

    val variacionVentas: Int?
        get() = if (anterior.centimos == 0L) {
            null
        } else {
            ((actual.centimos - anterior.centimos) * 100.0 / anterior.centimos).roundToInt()
        }
}
