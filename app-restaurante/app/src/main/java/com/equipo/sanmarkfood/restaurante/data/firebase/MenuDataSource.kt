package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MenuDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun categorias(uid: String) =
        firestore.collection("restaurantes").document(uid).collection("categorias")

    fun observarCategorias(uid: String): Flow<List<Categoria>> = callbackFlow {
        val registro = categorias(uid).orderBy("orden").addSnapshotListener { foto, error ->
            if (error != null) {
                close(error.aErrorMenu())
                return@addSnapshotListener
            }
            try {
                trySend(foto?.documents.orEmpty().map { it.aCategoria() })
            } catch (e: ErrorMenu) {
                close(e)
            }
        }
        awaitClose { registro.remove() }
    }

    suspend fun leerCategorias(uid: String): List<Categoria> =
        llamarFirebaseMenu {
            categorias(uid).orderBy("orden").get(Source.SERVER).await().documents.map { it.aCategoria() }
        }

    suspend fun crearCategoria(uid: String, nombre: String, orden: Int): Categoria =
        llamarFirebaseMenu {
            val documento = categorias(uid).document()
            documento.set(mapOf("nombre" to nombre, "orden" to orden)).await()
            Categoria(id = documento.id, nombre = nombre, orden = orden)
        }
}
