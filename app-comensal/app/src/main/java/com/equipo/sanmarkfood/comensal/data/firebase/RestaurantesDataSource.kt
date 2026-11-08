package com.equipo.sanmarkfood.comensal.data.firebase

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RestaurantesDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // Las reglas de Firestore solo dejan leer a un comensal los locales aprobados:
    // sin este filtro la consulta se rechaza.
    private fun aprobados(): Query =
        firestore.collection("restaurantes").whereEqualTo("estado", "aprobado")

    suspend fun obtenerAprobados(): List<Restaurante> =
        aprobados().get().await().documents.mapNotNull { it.aRestaurante() }

    fun observarAprobados(): Flow<List<Restaurante>> = callbackFlow {
        val registro = aprobados().addSnapshotListener { instantanea, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(instantanea?.documents.orEmpty().mapNotNull { it.aRestaurante() })
        }
        awaitClose { registro.remove() }
    }
}