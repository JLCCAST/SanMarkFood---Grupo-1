package com.equipo.sanmarkfood.comensal.data.firebase

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Pedido
import com.google.firebase.firestore.FirebaseFirestore
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
}
