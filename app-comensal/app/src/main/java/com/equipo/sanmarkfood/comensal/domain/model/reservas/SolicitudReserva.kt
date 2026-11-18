package com.equipo.sanmarkfood.comensal.domain.model.reservas

data class SolicitudReserva(
    val restauranteId: String,
    val fecha: String,
    val hora: String,
    val personas: Int,
)
