package com.equipo.sanmarkfood.comensal.domain.model.descubrimiento

sealed class ErrorDescubrimiento : Exception() {
    data object SinConexion : ErrorDescubrimiento()
    data object Desconocido : ErrorDescubrimiento()
}