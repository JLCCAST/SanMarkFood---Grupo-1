package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.firebase.AuthDataSource
import com.equipo.sanmarkfood.restaurante.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource
) : AuthRepository {

    override suspend fun registrar(correo: String, contrasena: String) =
        authDataSource.registrarYEnviarVerificacion(correo, contrasena)

    override suspend fun enviarVerificacion() = authDataSource.enviarVerificacion()

    override suspend fun correoVerificado(): Boolean = authDataSource.correoVerificado()
}
