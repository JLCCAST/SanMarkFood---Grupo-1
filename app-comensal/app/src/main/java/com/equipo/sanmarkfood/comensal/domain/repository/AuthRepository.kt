package com.equipo.sanmarkfood.comensal.domain.repository

import com.equipo.sanmarkfood.comensal.domain.model.auth.SesionUsuario

interface AuthRepository {
    val currentUser: SesionUsuario?
    suspend fun register(name: String, email: String, password: String): Result<Unit>
    suspend fun login(email: String, password: String): Result<SesionUsuario>
    suspend fun sendVerification(): Result<Unit>
    suspend fun reloadAndCheckVerified(): Result<Boolean>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    fun logout()
}