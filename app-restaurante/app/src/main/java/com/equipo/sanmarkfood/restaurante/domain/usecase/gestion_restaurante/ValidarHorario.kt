package com.equipo.sanmarkfood.restaurante.domain.usecase.gestion_restaurante

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Horario

/** Reglas del horario que comparten el alta (R4, enviar a revisión) y la edición (O8). */
internal fun validarHorario(horario: Horario) {
    if (horario.dias.values.none { it.abierto }) throw ErrorRestaurante.NingunDiaAbierto
    val horasInvalidas = horario.dias.filterValues { it.abierto && it.cierra <= it.abre }.keys
    if (horasInvalidas.isNotEmpty()) throw ErrorRestaurante.HorasInvalidas(horasInvalidas)
}
