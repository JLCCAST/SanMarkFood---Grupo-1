package com.equipo.sanmarkfood.comensal.domain.model.reservas

sealed class ErrorReservas : Exception() {
    data object SinConexion : ErrorReservas()
    data object SesionInvalida : ErrorReservas()
    data object FueraDePlazo : ErrorReservas()
    data object FueraDeHorario : ErrorReservas()
    data object FranjaLlena : ErrorReservas()
    data object TopeReservas : ErrorReservas()
    data object SinReservas : ErrorReservas()
    data object ComensalesInvalidos : ErrorReservas()
    data object SinCambios : ErrorReservas()
    data object EstadoInvalido : ErrorReservas()
    data object Desconocido : ErrorReservas()
}
