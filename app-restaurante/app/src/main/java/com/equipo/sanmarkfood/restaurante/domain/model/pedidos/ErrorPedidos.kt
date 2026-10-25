package com.equipo.sanmarkfood.restaurante.domain.model.pedidos

sealed class ErrorPedidos : Exception() {
    data object SinConexion : ErrorPedidos()
    data object YaCambio : ErrorPedidos()
    data object Desconocido : ErrorPedidos()
}
