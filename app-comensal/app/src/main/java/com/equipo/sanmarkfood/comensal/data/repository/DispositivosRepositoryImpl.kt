package com.equipo.sanmarkfood.comensal.data.repository

import com.equipo.sanmarkfood.comensal.domain.repository.DispositivosRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject

class DispositivosRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val messaging: FirebaseMessaging
) : DispositivosRepository {

    @Suppress("DEPRECATION")
    override fun registrar() {
        messaging.token.addOnSuccessListener { token -> renovar(token) }
    }

    override fun renovar(token: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("usuarios").document(uid)
            .collection("dispositivos").document(token)
            .set(
                mapOf(
                    "app" to "comensal",
                    "actualizadoEn" to FieldValue.serverTimestamp()
                )
            )
    }

    @Suppress("DEPRECATION")
    override fun olvidar() {
        messaging.deleteToken()
    }
}
