package com.equipo.sanmarkfood.comensal.data.firebase

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.CategoriaCarta
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MenuDeHoy
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Plato
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class CartaDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private fun local(restauranteId: String) = firestore.collection("restaurantes").document(restauranteId)

    fun observarCategorias(restauranteId: String): Flow<List<CategoriaCarta>> =
        observar(local(restauranteId).collection("categorias").orderBy("orden")) { it.aCategoriaCarta() }

    fun observarPlatos(restauranteId: String): Flow<List<Plato>> =
        observar(local(restauranteId).collection("platos")) { it.aPlato() }

    fun observarMenu(restauranteId: String, fecha: String): Flow<MenuDeHoy?> = callbackFlow {
        val registro = local(restauranteId).collection("menus").document(fecha)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.takeIf { it.exists() }?.aMenuDeHoy())
            }
        awaitClose { registro.remove() }
    }

    private fun <T : Any> observar(consulta: Query, mapear: (DocumentSnapshot) -> T?): Flow<List<T>> =
        callbackFlow {
            val registro = consulta.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) trySend(snapshot.documents.mapNotNull(mapear))
            }
            awaitClose { registro.remove() }
        }
}