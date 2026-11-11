package com.equipo.sanmarkfood.comensal.domain.model.pedidos

sealed class ErrorUbicacion : Exception() {
    data object SinConexion : ErrorUbicacion()
    data object NoEncontrada : ErrorUbicacion()
    data object Desconocido : ErrorUbicacion()
}
