package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.admin.DetalleSolicitud
import com.equipo.sanmarkfood.restaurante.domain.model.admin.ErrorAdmin
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Rechazo
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RevisionDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun restaurante(uid: String) = firestore.collection("restaurantes").document(uid)

    fun observar(uid: String): Flow<DetalleSolicitud?> =
        documento(uid).map { foto ->
            if (!foto.exists()) return@map null
            coroutineScope {
                val correo = async { leerCorreo(uid) }
                val platos = async { contar(uid, "platos") }
                val categorias = async { contar(uid, "categorias") }
                try {
                    foto.aDetalleSolicitud(correo.await(), platos.await(), categorias.await())
                } catch (e: ErrorRestaurante) {
                    throw ErrorAdmin.Desconocido
                }
            }
        }

    suspend fun aprobar(uid: String): Boolean =
        decidir(uid, mapOf("estado" to EstadoRestaurante.APROBADO.valor()))

    suspend fun rechazar(uid: String, rechazo: Rechazo): Boolean =
        decidir(
            uid,
            mapOf(
                "estado" to EstadoRestaurante.RECHAZADO.valor(),
                "rechazo" to rechazo.aCampos(),
            ),
        )

    private suspend fun decidir(uid: String, cambios: Map<String, Any>): Boolean = llamarFirebaseAdmin {
        firestore.runTransaction { transaccion ->
            val actual = transaccion.get(restaurante(uid))
            if (actual.getString("estado") != EstadoRestaurante.PENDIENTE.valor()) return@runTransaction false
            transaccion.update(restaurante(uid), cambios + ("revisadoEn" to FieldValue.serverTimestamp()))
            true
        }.await()
    }

    private fun documento(uid: String): Flow<DocumentSnapshot> = callbackFlow {
        val registro = restaurante(uid).addSnapshotListener { foto, error ->
            if (error != null) {
                close(error.aErrorAdmin())
                return@addSnapshotListener
            }
            if (foto != null) trySend(foto)
        }
        awaitClose { registro.remove() }
    }

    private suspend fun leerCorreo(uid: String): String? = try {
        firestore.collection("usuarios").document(uid).get().await().getString("correo")
    } catch (e: FirebaseException) {
        null
    }

    private suspend fun contar(uid: String, subcoleccion: String): Int = llamarFirebaseAdmin {
        restaurante(uid).collection(subcoleccion)
            .count()
            .get(AggregateSource.SERVER)
            .await()
            .count
            .toInt()
    }
}
