package com.equipo.sanmarkfood.restaurante.domain.model.admin

sealed class ErrorAdmin : Exception() {
    data object SinConexion : ErrorAdmin()
    data object MotivoFaltante : ErrorAdmin()
    data object DetalleObligatorio : ErrorAdmin()
    data object YaRevisada : ErrorAdmin()
    data object Desconocido : ErrorAdmin()
}
