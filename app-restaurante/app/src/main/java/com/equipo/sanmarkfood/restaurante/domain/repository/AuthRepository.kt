package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.auth.EstadoSesion

interface AuthRepository {
    suspend fun registrar(correo: String, contrasena: String)
    suspend fun enviarVerificacion()
    suspend fun correoVerificado(): Boolean
    suspend fun iniciarSesion(correo: String, contrasena: String)
    suspend fun iniciarSesionConGoogle(): Boolean
    suspend fun estadoSesion(): EstadoSesion
    fun cerrarSesion()
    suspend fun enviarRecuperacion(correo: String)
}
