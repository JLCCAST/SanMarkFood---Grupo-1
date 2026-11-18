package com.equipo.sanmarkfood.comensal.domain.model.resenas

data class Calificacion(
    val pedidoId: String,
    val estrellas: Int,
    val comentario: String?,
    val platos: Map<String, Boolean>,
) {
    companion object {
        const val MIN_ESTRELLAS = 1
        const val MAX_ESTRELLAS = 5
        const val MAX_COMENTARIO = 500
    }
}

sealed class ErrorCalificar : Exception() {
    data object SinConexion : ErrorCalificar()
    data object YaCalificado : ErrorCalificar()
    data object NoEntregado : ErrorCalificar()
    data object DatosInvalidos : ErrorCalificar()
    data object Desconocido : ErrorCalificar()
}
