package com.equipo.sanmarkfood.comensal.domain.model.pedidos

sealed class ErrorCrearPedido : Exception() {
    data object SinConexion : ErrorCrearPedido()
    data object SesionInvalida : ErrorCrearPedido()
    data object SinDireccion : ErrorCrearPedido()
    data object LocalNoDisponible : ErrorCrearPedido()
    data object LocalCerrado : ErrorCrearPedido()
    data object PlatoAgotado : ErrorCrearPedido()
    data object OpcionAgotada : ErrorCrearPedido()
    data object MenuVencido : ErrorCrearPedido()
    data object DatosInvalidos : ErrorCrearPedido()
    data object Desconocido : ErrorCrearPedido()
}
