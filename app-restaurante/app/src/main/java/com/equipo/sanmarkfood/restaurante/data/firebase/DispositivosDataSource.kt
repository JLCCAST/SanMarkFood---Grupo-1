package com.equipo.sanmarkfood.restaurante.data.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject

class DispositivosDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val messaging: FirebaseMessaging,
) {
    @Suppress("DEPRECATION")
    fun registrar(uid: String) {
        messaging.token.addOnSuccessListener { token -> guardar(uid, token) }
    }

    fun guardar(uid: String, token: String) {
        firestore.collection("usuarios").document(uid).collection("dispositivos").document(token).set(
            mapOf(
                "app" to "restaurante",
                "actualizadoEn" to FieldValue.serverTimestamp(),
            )
        )
    }

    @Suppress("DEPRECATION")
    fun olvidar() {
        messaging.deleteToken()
    }
}
