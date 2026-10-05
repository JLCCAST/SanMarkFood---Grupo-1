package com.equipo.sanmarkfood.restaurante.domain.model.admin

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante

enum class FiltroSolicitudes(val estado: EstadoRestaurante) {
    PENDIENTES(EstadoRestaurante.PENDIENTE),
    APROBADAS(EstadoRestaurante.APROBADO),
    RECHAZADAS(EstadoRestaurante.RECHAZADO),
}
