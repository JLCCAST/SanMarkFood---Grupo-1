package com.equipo.sanmarkfood.restaurante.domain.model.auth

sealed class ErrorAuth : Exception() {
    data object SinConexion : ErrorAuth()
    data object CamposVacios : ErrorAuth()
    data object CredencialesInvalidas : ErrorAuth()
    data object CorreoYaRegistrado : ErrorAuth()
    data object CorreoInvalido : ErrorAuth()
    data object CorreoSinVerificar : ErrorAuth()
    data object CuentaDeComensal : ErrorAuth()
    data object DemasiadosIntentos : ErrorAuth()
    data object Desconocido : ErrorAuth()
}
