package com.equipo.sanmarkfood.comensal.domain.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun register(name: String, email: String, password: String): Result<FirebaseUser>
    suspend fun login(email: String, password: String): Result<FirebaseUser>
    suspend fun sendVerification(): Result<Unit>
    suspend fun reloadAndCheckVerified(): Result<Boolean>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    fun logout()
}