package com.equipo.sanmarkfood.comensal.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    init {
        // Los correos que envía Firebase (verificación y recuperación) llegan en español.
        auth.setLanguageCode("es")
    }

    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun register(name: String, email: String, password: String): Result<FirebaseUser> =
        runCatching {
            val user = auth.createUserWithEmailAndPassword(email, password).await().user
                ?: error("No se pudo crear el usuario")

            runCatching {
                val profile = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profile).await()
            }
            runCatching { user.sendEmailVerification().await() }
            runCatching { crearDocumentoUsuario(user, email) }

            user
        }

    // Las reglas de Firestore exigen que usuarios/{uid} lleve el rol al crearse.
    private suspend fun crearDocumentoUsuario(user: FirebaseUser, email: String) {
        firestore.collection("usuarios").document(user.uid).set(
            mapOf(
                "correo" to email,
                "rol" to "comensal",
                "creadoEn" to FieldValue.serverTimestamp()
            )
        ).await()
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
            val verificado = auth.currentUser?.isEmailVerified == true

            if (verificado) {
                auth.currentUser?.getIdToken(true)?.await()
            }
            verificado
        }

    /** Envía el correo con el enlace para crear una nueva contraseña (SCRUM-82). */
    suspend fun sendPasswordReset(email: String): Result<Unit> =
        runCatching {
            auth.sendPasswordResetEmail(email).await()
            Unit
        }

    fun logout() = auth.signOut()
}