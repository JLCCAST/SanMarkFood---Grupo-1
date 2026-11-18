package com.equipo.sanmarkfood.comensal.data.firebase

import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ReservasDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    fun observarReservasDelComensal(comensalUid: String, cantidad: Long): Flow<List<Reserva>> = callbackFlow {
        val registro = firestore.collection("reservas")
            .whereEqualTo("comensalUid", comensalUid)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(cantidad)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents.orEmpty().mapNotNull { it.aReserva() })
            }
        awaitClose { registro.remove() }
    }

    fun observarReserva(reservaId: String): Flow<Reserva?> = callbackFlow {
        val registro = firestore.collection("reservas").document(reservaId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.takeIf { it.exists() }?.aReserva())
            }
        awaitClose { registro.remove() }
    }

    fun observarOcupacion(restauranteId: String, fecha: String): Flow<Map<String, Int>> = callbackFlow {
        val registro = firestore.collection("restaurantes").document(restauranteId).collection("franjas")
            .whereGreaterThanOrEqualTo(FieldPath.documentId(), "${fecha}_0000")
            .whereLessThanOrEqualTo(FieldPath.documentId(), "${fecha}_2359")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents.orEmpty().mapNotNull { it.aOcupacion() }.toMap())
            }
        awaitClose { registro.remove() }
    }
}
