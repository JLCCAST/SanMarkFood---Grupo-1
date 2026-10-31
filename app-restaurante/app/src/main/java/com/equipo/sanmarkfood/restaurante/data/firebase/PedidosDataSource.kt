package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.ErrorPedidos
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.PedidosDeHoy
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Date
import javax.inject.Inject

class PedidosDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    fun observarDesde(uid: String, desde: Date): Flow<PedidosDeHoy> = callbackFlow {
        val registro = firestore.collection("pedidos")
            .whereEqualTo("restauranteUid", uid)
            .whereGreaterThanOrEqualTo("creadoEn", Timestamp(desde))
            .orderBy("creadoEn")
            .addSnapshotListener(MetadataChanges.INCLUDE) { foto, error ->
                if (error != null) {
                    close(error.aErrorPedidos())
                    return@addSnapshotListener
                }
                if (foto == null) return@addSnapshotListener
                try {
                    trySend(PedidosDeHoy(foto.documents.map { it.aPedido() }, sinConexion = foto.metadata.isFromCache))
                } catch (e: ErrorPedidos) {
                    close(e)
                }
            }
        awaitClose { registro.remove() }
    }
}
