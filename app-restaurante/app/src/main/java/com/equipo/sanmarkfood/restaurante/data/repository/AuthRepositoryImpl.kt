package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.firebase.AuthDataSource
import com.equipo.sanmarkfood.restaurante.domain.model.EstadoSesion
import com.equipo.sanmarkfood.restaurante.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource
) : AuthRepository {

    override suspend fun registrar(correo: String, contrasena: String) =
        authDataSource.registrarYEnviarVerificacion(correo, contrasena)

    override suspend fun enviarVerificacion() = authDataSource.enviarVerificacion()

    override suspend fun correoVerificado(): Boolean = authDataSource.correoVerificado()

    override suspend fun iniciarSesion(correo: String, contrasena: String) =
        authDataSource.iniciarSesion(correo, contrasena)

    override suspend fun estadoSesion(): EstadoSesion {
        val usuario = authDataSource.usuarioActual() ?: return EstadoSesion.SinSesion
        if (!usuario.isEmailVerified) return EstadoSesion.SinVerificar(usuario.email.orEmpty())
        return EstadoSesion.Activa
    }

    override fun cerrarSesion() = authDataSource.cerrarSesion()
}
