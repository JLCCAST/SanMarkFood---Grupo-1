package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.ErrorAuth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {
    suspend fun registrarYEnviarVerificacion(correo: String, contrasena: String): String {
        val resultado = llamarFirebase { auth.createUserWithEmailAndPassword(correo, contrasena).await() }
        val usuario = resultado.user ?: throw ErrorAuth.Desconocido
        usuario.sendEmailVerification()
        return usuario.uid
    }

    suspend fun enviarVerificacion() {
        llamarFirebase { auth.currentUser?.sendEmailVerification()?.await() }
    }

    suspend fun correoVerificado(): Boolean = llamarFirebase {
        auth.currentUser?.reload()?.await()
        auth.currentUser?.isEmailVerified == true
    }

    suspend fun iniciarSesion(correo: String, contrasena: String) {
        llamarFirebase { auth.signInWithEmailAndPassword(correo, contrasena).await() }
    }

    fun usuarioActual(): FirebaseUser? = auth.currentUser

    fun cerrarSesion() = auth.signOut()

    suspend fun enviarRecuperacion(correo: String) {
        llamarFirebase { auth.sendPasswordResetEmail(correo).await() }
    }
}
