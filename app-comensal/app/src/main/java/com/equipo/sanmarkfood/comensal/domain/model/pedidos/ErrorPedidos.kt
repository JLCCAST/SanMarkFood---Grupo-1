package com.equipo.sanmarkfood.comensal.domain.model.pedidos

sealed class ErrorPedidos : Exception() {
    data object SinConexion : ErrorPedidos()
    data object Desconocido : ErrorPedidos()
}