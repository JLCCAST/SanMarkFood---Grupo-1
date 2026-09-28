package com.equipo.sanmarkfood.restaurante.domain.model

data class Restaurante(
    val datos: DatosLocal,
    val estado: EstadoRestaurante,
    /** null mientras está en borrador: el horario se guarda al enviarlo a revisión (R4). */
    val horario: Horario?,
    /** Solo cuando está rechazado: el motivo que eligió el administrador. */
    val rechazo: Rechazo?,
)
