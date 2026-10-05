package com.equipo.sanmarkfood.restaurante.domain.model.admin

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Rechazo

data class SolicitudLocal(
    val uid: String,
    val nombre: String,
    val categoria: CategoriaRestaurante,
    val logoUrl: String?,
    val estado: EstadoRestaurante,
    val enviadoEn: Long,
    val revisadoEn: Long?,
    val reenviado: Boolean,
    val actualizacion: Boolean,
    val rechazoAnterior: Rechazo?,
    val cantidadPlatos: Int,
)
