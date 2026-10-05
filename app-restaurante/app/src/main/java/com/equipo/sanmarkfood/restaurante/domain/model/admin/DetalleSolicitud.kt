package com.equipo.sanmarkfood.restaurante.domain.model.admin

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Rechazo
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Restaurante

data class DetalleSolicitud(
    val uid: String,
    val restaurante: Restaurante,
    val correo: String?,
    val reenviado: Boolean,
    val rechazoAnterior: Rechazo?,
    val camposCorregidos: Set<CampoCorregido>,
    val revisadoEn: Long?,
    val cantidadPlatos: Int,
    val cantidadCategorias: Int,
)

enum class CampoCorregido {
    NOMBRE,
    CATEGORIA,
    DIRECCION,
    UBICACION,
    TELEFONO,
    PORTADA,
    LOGO,
}
