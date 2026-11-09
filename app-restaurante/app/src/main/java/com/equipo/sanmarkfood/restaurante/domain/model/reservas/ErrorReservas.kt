package com.equipo.sanmarkfood.restaurante.domain.model.reservas

sealed class ErrorReservas : Exception() {
    data object SinConexion : ErrorReservas()
    data object Desconocido : ErrorReservas()
}
