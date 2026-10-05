package com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante

/** Lo que el administrador elige al rechazar un registro (A2, HU24). El detalle es obligatorio con «Otro». */
data class Rechazo(val motivos: Set<MotivoRechazo>, val detalle: String?)

enum class MotivoRechazo {
    DATOS_INCOMPLETOS,
    DIRECCION_NO_VERIFICABLE,
    LOCAL_DUPLICADO,
    OTRO,
}
