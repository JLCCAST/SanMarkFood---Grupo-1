package com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante

const val LARGO_CODIGO_SMS = 6

sealed interface EventoVerificacion {
    data object CodigoEnviado : EventoVerificacion
    data class CodigoRecibido(val codigo: String) : EventoVerificacion
    data object Verificado : EventoVerificacion
}
