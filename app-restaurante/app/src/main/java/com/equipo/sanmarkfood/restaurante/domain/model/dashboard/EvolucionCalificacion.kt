package com.equipo.sanmarkfood.restaurante.domain.model.dashboard

import java.util.Date
import kotlin.math.roundToInt

data class PuntoCalificacion(val hasta: Date, val promedio: Double)

data class EvolucionCalificacion(val puntos: List<PuntoCalificacion>, val semanas: Int) {
    val actual: Double?
        get() = puntos.lastOrNull()?.promedio

    val cambio: Double?
        get() = if (puntos.size < 2) null else ((puntos.last().promedio - puntos.first().promedio) * 10).roundToInt() / 10.0
}
