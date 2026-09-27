package com.equipo.sanmarkfood.restaurante.domain.repository
interface AuthRepository {
    suspend fun registrar(correo: String, contrasena: String)
    suspend fun enviarVerificacion()
    suspend fun correoVerificado(): Boolean
}
