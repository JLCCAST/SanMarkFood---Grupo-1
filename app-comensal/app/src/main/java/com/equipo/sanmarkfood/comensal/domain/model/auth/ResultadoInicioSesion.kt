package com.equipo.sanmarkfood.comensal.domain.model.auth

import androidx.annotation.StringRes

/** Lo que puede pasar al iniciar sesión; el ViewModel decide qué mostrar en cada caso. */
sealed interface ResultadoInicioSesion {
    /** Sesión abierta y correo verificado. */
    data object Autenticado : ResultadoInicioSesion

    /** Sesión abierta, pero el correo todavía no se verificó. */
    data class PorVerificar(val correo: String) : ResultadoInicioSesion

    /** Los datos escritos no pasan las reglas; no se llamó a Firebase. */
    data class CampoInvalido(@StringRes val mensaje: Int) : ResultadoInicioSesion

    /** Firebase rechazó el inicio de sesión (contraseña incorrecta, cuenta de otro rol, etc.). */
    data class Fallo(val causa: Throwable) : ResultadoInicioSesion
}