package com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante

enum class EstadoRestaurante {
    /** Alta a medio camino (R3 guardado, R4 sin enviar). El administrador todavía no lo ve. */
    BORRADOR,
    PENDIENTE,
    APROBADO,
    RECHAZADO,
}
