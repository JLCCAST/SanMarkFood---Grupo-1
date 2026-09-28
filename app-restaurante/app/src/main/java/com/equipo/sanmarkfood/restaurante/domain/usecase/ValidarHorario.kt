package com.equipo.sanmarkfood.restaurante.domain.usecase

import com.equipo.sanmarkfood.restaurante.domain.model.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Horario

/** Reglas del horario que comparten el alta (R4, enviar a revisión) y la edición (O8). */
internal fun validarHorario(horario: Horario) {
    if (horario.dias.values.none { it.abierto }) throw ErrorRestaurante.NingunDiaAbierto
    val horasInvalidas = horario.dias.filterValues { it.abierto && it.cierra <= it.abre }.keys
    if (horasInvalidas.isNotEmpty()) throw ErrorRestaurante.HorasInvalidas(horasInvalidas)
}
