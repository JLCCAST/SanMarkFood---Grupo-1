package com.equipo.sanmarkfood.restaurante.domain.model.auth

sealed interface EstadoSesion {
    data object SinSesion : EstadoSesion
    data class SinVerificar(val correo: String) : EstadoSesion
    data class Activa(val rol: Rol) : EstadoSesion

    /** Cuenta de restaurante verificada que todavía no termina el alta de su local (no existe o está en borrador). */
    data object SinLocal : EstadoSesion
}
