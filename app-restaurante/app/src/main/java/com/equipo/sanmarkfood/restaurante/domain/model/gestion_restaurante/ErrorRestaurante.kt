package com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante

sealed class ErrorRestaurante : Exception() {
    data object SinConexion : ErrorRestaurante()
    data class DatosInvalidos(val campos: Set<CampoLocal>) : ErrorRestaurante()
    data object NingunDiaAbierto : ErrorRestaurante()
    data class HorasInvalidas(val dias: Set<DiaSemana>) : ErrorRestaurante()
    data object ImagenIlegible : ErrorRestaurante()
    data object Desconocido : ErrorRestaurante()
}
