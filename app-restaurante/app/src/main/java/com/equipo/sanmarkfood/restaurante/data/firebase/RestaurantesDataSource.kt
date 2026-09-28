package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.Restaurante
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RestaurantesDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun documento(uid: String) = firestore.collection("restaurantes").document(uid)

    suspend fun leer(uid: String): Restaurante? =
        llamarFirebaseRestaurante { documento(uid).get().await().aRestaurante() }

    // Sin conexión sigue emitiendo desde la caché, y se pone al día apenas vuelve la conexión.
    fun observar(uid: String): Flow<Restaurante?> = callbackFlow {
        val registro = documento(uid).addSnapshotListener { foto, error ->
            if (error != null) {
                close(error.aErrorRestaurante())
                return@addSnapshotListener
            }
            try {
                trySend(foto?.aRestaurante())
            } catch (e: ErrorRestaurante) {
                close(e)
            }
        }
        awaitClose { registro.remove() }
    }

    // Pregunta al servidor y no a la caché: sin conexión una escritura no falla, se queda esperando,
    // así que esta lectura es la que avisa que no hay internet antes de guardar.
    suspend fun existeEnServidor(uid: String): Boolean =
        llamarFirebaseRestaurante { documento(uid).get(Source.SERVER).await().exists() }

    suspend fun crear(uid: String, datos: DatosLocal, estado: EstadoRestaurante) {
        llamarFirebaseRestaurante {
            documento(uid).set(
                datos.aCampos() + mapOf(
                    "estado" to estado.valor(),
                    "creadoEn" to FieldValue.serverTimestamp(),
                    "actualizadoEn" to FieldValue.serverTimestamp(),
                )
            ).await()
        }
    }

    suspend fun actualizarDatos(uid: String, datos: DatosLocal) {
        llamarFirebaseRestaurante {
            documento(uid).update(datos.aCampos() + ("actualizadoEn" to FieldValue.serverTimestamp())).await()
        }
    }

    suspend fun enviarARevision(uid: String, horario: Horario) {
        llamarFirebaseRestaurante {
            documento(uid).update(
                mapOf(
                    "horario" to horario.aCampos(),
                    "estado" to EstadoRestaurante.PENDIENTE.valor(),
                    "enviadoEn" to FieldValue.serverTimestamp(),
                    "actualizadoEn" to FieldValue.serverTimestamp(),
                )
            ).await()
        }
    }

    suspend fun actualizarHorario(uid: String, horario: Horario) {
        llamarFirebaseRestaurante {
            documento(uid).update(
                mapOf(
                    "horario" to horario.aCampos(),
                    "actualizadoEn" to FieldValue.serverTimestamp(),
                )
            ).await()
        }
    }
}
