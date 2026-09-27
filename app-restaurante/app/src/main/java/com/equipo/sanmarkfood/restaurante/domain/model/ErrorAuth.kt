package com.equipo.sanmarkfood.restaurante.domain.model

sealed class ErrorAuth : Exception() {
    data object SinConexion : ErrorAuth()
    data object CorreoYaRegistrado : ErrorAuth()
    data object CorreoInvalido : ErrorAuth()
    data object CorreoSinVerificar : ErrorAuth()
    data object DemasiadosIntentos : ErrorAuth()
    data object Desconocido : ErrorAuth()
}
