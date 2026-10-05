package com.equipo.sanmarkfood.comensal.domain.model.auth

/** Datos de la cuenta con sesión abierta, sin depender de Firebase. */
data class SesionUsuario(
    val uid: String,
    val correo: String,
    val nombre: String,
    val correoVerificado: Boolean,
    /** Momento de creación de la cuenta, en milisegundos (null si no se conoce). */
    val fechaCreacion: Long?
)