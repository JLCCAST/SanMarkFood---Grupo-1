package com.equipo.sanmarkfood.comensal.domain.model.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.EstadoMenuDelDia

data class OpcionMenu(
    val nombre: String,
    val agotado: Boolean,
)

data class MenuDeHoy(
    val fecha: String,
    val precio: Int,
    val entradas: List<OpcionMenu>,
    val segundos: List<OpcionMenu>,
    val refresco: String?,
    val postre: String?,
    val horaFin: String,
    val estado: EstadoMenuDelDia,
)
