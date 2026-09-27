package com.equipo.sanmarkfood.restaurante.data.firebase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {
    suspend fun registrarYEnviarVerificacion(correo: String, contrasena: String) {
        val resultado = llamarFirebase { auth.createUserWithEmailAndPassword(correo, contrasena).await() }
        resultado.user?.sendEmailVerification()
    }

    suspend fun enviarVerificacion() {
        llamarFirebase { auth.currentUser?.sendEmailVerification()?.await() }
    }

    suspend fun correoVerificado(): Boolean = llamarFirebase {
        auth.currentUser?.reload()?.await()
        auth.currentUser?.isEmailVerified == true
    }
}
