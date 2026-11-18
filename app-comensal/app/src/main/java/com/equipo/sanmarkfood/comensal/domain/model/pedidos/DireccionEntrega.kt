package com.equipo.sanmarkfood.comensal.domain.model.pedidos

data class DireccionEntrega(
    val texto: String,
    val latitud: Double,
    val longitud: Double,
)