package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.ReservasDeLaSemana
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ReservasDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    fun observarEntre(uid: String, desde: String, hasta: String): Flow<ReservasDeLaSemana> = callbackFlow {
        val registro = firestore.collection("reservas")
            .whereEqualTo("restauranteUid", uid)
            .whereGreaterThanOrEqualTo("fecha", desde)
            .whereLessThanOrEqualTo("fecha", hasta)
            .orderBy("fecha")
            .orderBy("hora")
            .addSnapshotListener(MetadataChanges.INCLUDE) { foto, error ->
                if (error != null) {
                    close(error.aErrorReservas())
                    return@addSnapshotListener
                }
                if (foto == null) return@addSnapshotListener
                try {
                    trySend(ReservasDeLaSemana(foto.documents.map { it.aReserva() }, sinConexion = foto.metadata.isFromCache))
                } catch (e: ErrorReservas) {
                    close(e)
                }
            }
        awaitClose { registro.remove() }
    }
}
