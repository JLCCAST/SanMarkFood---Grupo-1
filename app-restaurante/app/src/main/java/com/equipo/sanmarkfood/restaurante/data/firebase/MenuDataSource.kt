package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosPlato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.Plato
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MenuDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun restaurante(uid: String) = firestore.collection("restaurantes").document(uid)

    private fun categorias(uid: String) = restaurante(uid).collection("categorias")

    private fun platos(uid: String) = restaurante(uid).collection("platos")

    fun observarCategorias(uid: String): Flow<List<Categoria>> =
        observar(categorias(uid).orderBy("orden")) { it.aCategoria() }

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

    fun observarPlatos(uid: String): Flow<List<Plato>> = observar(platos(uid)) { it.aPlato() }

    fun nuevoIdPlato(uid: String): String = platos(uid).document().id

    suspend fun crearPlato(uid: String, id: String, datos: DatosPlato, fotoUrl: String?) {
        val campos = buildMap {
            putAll(datos.aCampos())
            fotoUrl?.let { put("fotoUrl", it) }
            put("creadoEn", FieldValue.serverTimestamp())
            put("actualizadoEn", FieldValue.serverTimestamp())
        }
        llamarFirebaseMenu { platos(uid).document(id).set(campos).await() }
    }

    suspend fun leerPrecios(uid: String): List<Int> =
        llamarFirebaseMenu {
            platos(uid).get(Source.SERVER).await().documents.mapNotNull { it.getLong("precio")?.toInt() }
        }

    suspend fun guardarRangoCarta(uid: String, min: Int, max: Int) {
        llamarFirebaseMenu {
            restaurante(uid).update(
                mapOf(
                    "rangoCarta" to mapOf("min" to min, "max" to max),
                    "actualizadoEn" to FieldValue.serverTimestamp(),
                )
            ).await()
        }
    }

    private fun <T> observar(consulta: Query, mapear: (DocumentSnapshot) -> T): Flow<List<T>> = callbackFlow {
        val registro = consulta.addSnapshotListener { foto, error ->
            if (error != null) {
                close(error.aErrorMenu())
                return@addSnapshotListener
            }
            try {
                trySend(foto?.documents.orEmpty().map(mapear))
            } catch (e: ErrorMenu) {
                close(e)
            }
        }
        awaitClose { registro.remove() }
    }
}
