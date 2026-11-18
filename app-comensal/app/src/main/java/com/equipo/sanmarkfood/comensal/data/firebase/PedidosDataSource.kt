package com.equipo.sanmarkfood.comensal.data.firebase

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Pedido
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class PedidosDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    fun observarPedido(pedidoId: String): Flow<Pedido?> = callbackFlow {
        val registro = firestore.collection("pedidos").document(pedidoId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.takeIf { it.exists() }?.aPedido())
            }
        awaitClose { registro.remove() }
    }

    fun observarPedidosRecientes(comensalUid: String, cantidad: Long): Flow<List<Pedido>> = callbackFlow {
        val registro = firestore.collection("pedidos")
            .whereEqualTo("comensalUid", comensalUid)
            .orderBy("creadoEn", Query.Direction.DESCENDING)
            .limit(cantidad)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents.orEmpty().mapNotNull { it.aPedido() })
            }
        awaitClose { registro.remove() }
    }
}
