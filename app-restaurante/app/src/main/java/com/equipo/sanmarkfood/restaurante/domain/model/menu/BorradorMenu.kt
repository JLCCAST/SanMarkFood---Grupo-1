package com.equipo.sanmarkfood.restaurante.domain.model.menu

data class BorradorMenu(
    val origen: OrigenMenu,
    val precio: String,
    val entradas: List<String>,
    val segundos: List<String>,
    val refresco: String,
    val postre: String,
    val horaFin: String,
)
