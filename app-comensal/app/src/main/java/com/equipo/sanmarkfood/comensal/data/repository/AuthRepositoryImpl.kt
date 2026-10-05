package com.equipo.sanmarkfood.comensal.data.repository

import com.equipo.sanmarkfood.comensal.domain.model.auth.CuentaDeOtroRolException
import com.equipo.sanmarkfood.comensal.domain.model.auth.SesionUsuario
import com.equipo.sanmarkfood.comensal.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    init {
        // Los correos que envía Firebase (verificación y recuperación) llegan en español.
        auth.setLanguageCode("es")
    }

    override val currentUser: SesionUsuario? get() = auth.currentUser?.aSesionUsuario()

    /** Convierte el usuario de Firebase al modelo de dominio. */
    private fun FirebaseUser.aSesionUsuario() = SesionUsuario(
        uid = uid,
        correo = email.orEmpty(),
        nombre = displayName.orEmpty(),
        correoVerificado = isEmailVerified,
        fechaCreacion = metadata?.creationTimestamp
    )

    override suspend fun register(name: String, email: String, password: String): Result<Unit> =
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

            Unit
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

    override suspend fun login(email: String, password: String): Result<SesionUsuario> =
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
            user.aSesionUsuario()
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