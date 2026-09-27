package com.equipo.sanmarkfood.restaurante.data.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UsuariosDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun documento(uid: String) = firestore.collection("usuarios").document(uid)

    fun crearRestaurante(uid: String, correo: String) {
        documento(uid).set(
            mapOf(
                "rol" to "restaurante",
                "correo" to correo,
                "creadoEn" to FieldValue.serverTimestamp(),
            )
        )
    }

    suspend fun leerRol(uid: String): String? =
        llamarFirebase { documento(uid).get().await().getString("rol") }
}
