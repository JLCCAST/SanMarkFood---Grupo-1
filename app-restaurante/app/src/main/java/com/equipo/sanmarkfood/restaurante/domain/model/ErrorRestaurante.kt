package com.equipo.sanmarkfood.restaurante.domain.model

sealed class ErrorRestaurante : Exception() {
    data object SinConexion : ErrorRestaurante()
    data class DatosInvalidos(val campos: Set<CampoLocal>) : ErrorRestaurante()
    data object Desconocido : ErrorRestaurante()
}
