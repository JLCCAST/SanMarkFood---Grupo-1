package com.equipo.sanmarkfood.comensal.domain.model.pedidos

sealed class ErrorCancelarPedido : Exception() {
    data object SinConexion : ErrorCancelarPedido()
    data object YaAceptado : ErrorCancelarPedido()
    data object Desconocido : ErrorCancelarPedido()
}
