package com.equipo.sanmarkfood.restaurante.domain.model

sealed interface EstadoSesion {
    data object SinSesion : EstadoSesion
    data class SinVerificar(val correo: String) : EstadoSesion
    data class Activa(val rol: Rol) : EstadoSesion

    /** Cuenta de restaurante verificada que todavía no registra su local. */
    data object SinLocal : EstadoSesion
}
