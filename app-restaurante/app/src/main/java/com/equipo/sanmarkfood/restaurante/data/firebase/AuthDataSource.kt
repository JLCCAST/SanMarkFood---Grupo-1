package com.equipo.sanmarkfood.restaurante.data.firebase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {
    suspend fun registrar(correo: String, contrasena: String) {
        llamarFirebase { auth.createUserWithEmailAndPassword(correo, contrasena).await() }
    }
}
