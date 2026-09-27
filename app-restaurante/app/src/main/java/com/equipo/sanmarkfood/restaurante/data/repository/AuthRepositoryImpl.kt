package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.firebase.AuthDataSource
import com.equipo.sanmarkfood.restaurante.data.firebase.UsuariosDataSource
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorAuth
import com.equipo.sanmarkfood.restaurante.domain.model.EstadoSesion
import com.equipo.sanmarkfood.restaurante.domain.model.Rol
import com.equipo.sanmarkfood.restaurante.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val usuariosDataSource: UsuariosDataSource,
) : AuthRepository {

    override suspend fun registrar(correo: String, contrasena: String) {
        val uid = authDataSource.registrarYEnviarVerificacion(correo, contrasena)
        usuariosDataSource.crearRestaurante(uid, correo)
    }

    override suspend fun enviarVerificacion() = authDataSource.enviarVerificacion()

    override suspend fun correoVerificado(): Boolean = authDataSource.correoVerificado()

    override suspend fun iniciarSesion(correo: String, contrasena: String) =
        authDataSource.iniciarSesion(correo, contrasena)

    override suspend fun estadoSesion(): EstadoSesion {
        val usuario = authDataSource.usuarioActual() ?: return EstadoSesion.SinSesion
        if (!usuario.isEmailVerified) return EstadoSesion.SinVerificar(usuario.email.orEmpty())

        val rol = usuariosDataSource.leerRol(usuario.uid)
        if (rol == null) {
            usuariosDataSource.crearRestaurante(usuario.uid, usuario.email.orEmpty())
            return EstadoSesion.Activa(Rol.RESTAURANTE)
        }
        return EstadoSesion.Activa(aRol(rol))
    }

    override fun cerrarSesion() = authDataSource.cerrarSesion()

    override suspend fun enviarRecuperacion(correo: String) = authDataSource.enviarRecuperacion(correo)

    private fun aRol(valor: String): Rol = when (valor) {
        "comensal" -> Rol.COMENSAL
        "restaurante" -> Rol.RESTAURANTE
        "administrador" -> Rol.ADMINISTRADOR
        else -> throw ErrorAuth.Desconocido
    }
}
