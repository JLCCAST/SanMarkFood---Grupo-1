package com.equipo.sanmarkfood.comensal.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun register(name: String, email: String, password: String): Result<FirebaseUser> =
        runCatching {
            val user = auth.createUserWithEmailAndPassword(email, password).await().user
                ?: error("No se pudo crear el usuario")

            // Si alguno de estos dos pasos falla, la cuenta ya existe: no debe
            // fallar el registro. El nombre se puede completar después y el
            // correo de verificación se puede reenviar desde la pantalla.
            runCatching {
                val profile = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profile).await()
            }
            runCatching { user.sendEmailVerification().await() }

            user
        }

    suspend fun login(email: String, password: String): Result<FirebaseUser> =
        runCatching {
            auth.signInWithEmailAndPassword(email, password).await().user
                ?: error("No se pudo iniciar sesión")
        }

    suspend fun sendVerification(): Result<Unit> =
        runCatching {
            val user = auth.currentUser ?: error("No hay sesión activa")
            user.sendEmailVerification().await()
            Unit
        }

    suspend fun reloadAndCheckVerified(): Result<Boolean> =
        runCatching {
            val user = auth.currentUser ?: error("No hay sesión activa")
            user.reload().await()
            auth.currentUser?.isEmailVerified == true
        }

    fun logout() = auth.signOut()
}