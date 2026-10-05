package com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante

data class Restaurante(
    val datos: DatosLocal,
    val estado: EstadoRestaurante,
    /** null mientras está en borrador: el horario se guarda al enviarlo a revisión (R4). */
    val horario: Horario?,
    /** Solo cuando está rechazado: el motivo que eligió el administrador. */
    val rechazo: Rechazo?,
    /** Pausa de pedidos (SCRUM-66): el local aprobado sigue en el mapa, pero como «Cerrado». */
    val pausado: Boolean,
) {
    fun requiereNuevaRevision(editados: DatosLocal): Boolean =
        estado == EstadoRestaurante.APROBADO && datos.cambiaCamposSensibles(editados)
}
