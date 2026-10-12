package com.equipo.sanmarkfood.comensal.domain.model.auth

/** Lo que puede pasar al registrar una cuenta; el ViewModel decide qué mostrar en cada caso. */
sealed interface ResultadoRegistro {
    /** Cuenta creada; falta verificar el correo (el envío ya se pidió). */
    data class Registrado(val correo: String) : ResultadoRegistro

    /** Los datos escritos no pasan las reglas; no se llamó a Firebase. */
    data class CampoInvalido(val error: ErrorAuth) : ResultadoRegistro

    /** El registro falló (correo ya registrado, sin conexión, etc.). */
    data class Fallo(val error: ErrorAuth) : ResultadoRegistro
}