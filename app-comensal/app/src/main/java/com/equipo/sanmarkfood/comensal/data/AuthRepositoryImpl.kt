package com.equipo.sanmarkfood.comensal.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/** La cuenta existe, pero su rol en usuarios/{uid} no es "comensal" (D5). */
class CuentaDeOtroRolException : Exception("La cuenta no es de comensal")

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun register(name: String, email: String, password: String): Result<FirebaseUser>
    suspend fun login(email: String, password: String): Result<FirebaseUser>
    suspend fun sendVerification(): Result<Unit>
    suspend fun reloadAndCheckVerified(): Result<Boolean>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    fun logout()
}

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    init {
        // Los correos que envía Firebase (verificación y recuperación) llegan en español.
        auth.setLanguageCode("es")
    }

    override val currentUser: FirebaseUser? get() = auth.currentUser

    override suspend fun register(name: String, email: String, password: String): Result<FirebaseUser> =
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

    override suspend fun login(email: String, password: String): Result<FirebaseUser> =
        runCatching {
            val user = auth.signInWithEmailAndPassword(email, password).await().user
                ?: error("No se pudo iniciar sesión")

            try {
                verificarRolComensal(user)
            } catch (e: Exception) {
                // Si la cuenta no es de comensal (o no se pudo comprobar), no se deja la sesión abierta.
                auth.signOut()
                throw e
            }
            user
        }

    /**
     * D5: esta app solo admite cuentas con rol "comensal". Si la cuenta no tiene
     * documento en usuarios, se crea con ese rol (cuentas anteriores a HU05).
     */
    private suspend fun verificarRolComensal(user: FirebaseUser) {
        val documento = firestore.collection("usuarios").document(user.uid).get().await()
        when {
            !documento.exists() -> crearDocumentoUsuario(user, user.email.orEmpty())
            documento.getString("rol") != "comensal" -> throw CuentaDeOtroRolException()
        }
    }

    override suspend fun sendVerification(): Result<Unit> =
        runCatching {
            val user = auth.currentUser ?: error("No hay sesión activa")
            user.sendEmailVerification().await()
            Unit
        }

    override suspend fun reloadAndCheckVerified(): Result<Boolean> =
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
    override suspend fun sendPasswordReset(email: String): Result<Unit> =
        runCatching {
            auth.sendPasswordResetEmail(email).await()
            Unit
        }

    override fun logout() = auth.signOut()
}