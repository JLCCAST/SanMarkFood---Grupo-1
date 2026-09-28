package com.equipo.sanmarkfood.restaurante.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RestaurantesDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun documento(uid: String) = firestore.collection("restaurantes").document(uid)

    suspend fun existe(uid: String): Boolean =
        llamarFirebase { documento(uid).get().await().exists() }
}
