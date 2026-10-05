package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.admin.SolicitudLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SolicitudesDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun restaurantes() = firestore.collection("restaurantes")

    fun observarPendientes(): Flow<List<SolicitudLocal>> =
        documentosPendientes().map { documentos ->
            coroutineScope {
                documentos
                    .map { documento -> async { documento.aSolicitudLocal(contarPlatos(documento.id)) } }
                    .awaitAll()
            }
        }

    private fun documentosPendientes(): Flow<List<DocumentSnapshot>> = callbackFlow {
        val registro = restaurantes()
            .whereEqualTo("estado", EstadoRestaurante.PENDIENTE.valor())
            .addSnapshotListener { fotos, error ->
                if (error != null) {
                    close(error.aErrorAdmin())
                    return@addSnapshotListener
                }
                trySend(fotos?.documents.orEmpty())
            }
        awaitClose { registro.remove() }
    }

    private suspend fun contarPlatos(uid: String): Int = llamarFirebaseAdmin {
        restaurantes().document(uid).collection("platos")
            .count()
            .get(AggregateSource.SERVER)
            .await()
            .count
            .toInt()
    }
}
