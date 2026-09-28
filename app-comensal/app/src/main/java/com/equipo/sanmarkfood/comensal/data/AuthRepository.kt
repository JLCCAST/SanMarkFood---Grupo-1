package com.equipo.sanmarkfood.comensal.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun register(email: String, password: String): Result<FirebaseUser> =
        runCatching {
            auth.createUserWithEmailAndPassword(email, password).await().user
                ?: error("No se pudo crear el usuario")
        }

    suspend fun login(email: String, password: String): Result<FirebaseUser> =
        runCatching {
            auth.signInWithEmailAndPassword(email, password).await().user
                ?: error("No se pudo iniciar sesión")
        }

    fun logout() = auth.signOut()
}