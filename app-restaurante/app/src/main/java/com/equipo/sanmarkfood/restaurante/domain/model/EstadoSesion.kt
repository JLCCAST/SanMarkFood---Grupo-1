package com.equipo.sanmarkfood.restaurante.domain.model

sealed interface EstadoSesion {
    data object SinSesion : EstadoSesion
    data class SinVerificar(val correo: String) : EstadoSesion
    data object Activa : EstadoSesion
}
