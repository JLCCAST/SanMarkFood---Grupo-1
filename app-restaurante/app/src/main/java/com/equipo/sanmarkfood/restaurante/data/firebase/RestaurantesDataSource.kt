package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Restaurante
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

    suspend fun actualizarDatosYPedirRevision(uid: String, datos: DatosLocal): Boolean =
        llamarFirebaseRestaurante {
            firestore.runTransaction { transaccion ->
                val actual = transaccion.get(documento(uid))
                if (actual.getString("estado") != EstadoRestaurante.APROBADO.valor()) return@runTransaction false

                val nuevos = datos.aCampos()
                val cambiados = nuevos.filter { (campo, valor) -> actual.get(campo) != valor }.keys.toList()
                transaccion.update(
                    documento(uid),
                    nuevos + mapOf(
                        "estado" to EstadoRestaurante.PENDIENTE.valor(),
                        "camposCorregidos" to cambiados,
                        "reenviado" to FieldValue.delete(),
                        "rechazoAnterior" to FieldValue.delete(),
                        "enviadoEn" to FieldValue.serverTimestamp(),
                        "actualizadoEn" to FieldValue.serverTimestamp(),
                    ),
                )
                true
            }.await()
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

    /**
     * R7 (SCRUM-160). En una transacción, que además falla sin conexión en vez de quedar en espera:
     * - guarda los datos corregidos y pasa el local de rechazado a pendiente;
     * - copia el rechazo tal cual a rechazoAnterior, para que el administrador vea el motivo;
     * - marca reenviado y anota en camposCorregidos qué campos cambiaron («Cambió desde entonces…» en A2).
     * Devuelve false si el local ya no estaba rechazado.
     */
    suspend fun reenviarARevision(uid: String, datos: DatosLocal): Boolean =
        llamarFirebaseRestaurante {
            firestore.runTransaction { transaccion ->
                val actual = transaccion.get(documento(uid))
                if (actual.getString("estado") != EstadoRestaurante.RECHAZADO.valor()) return@runTransaction false

                val corregidos = datos.aCampos()
                val camposCorregidos = corregidos.filter { (campo, valor) -> actual.get(campo) != valor }.keys.toList()
                transaccion.update(
                    documento(uid),
                    corregidos + mapOf(
                        "estado" to EstadoRestaurante.PENDIENTE.valor(),
                        "rechazo" to FieldValue.delete(),
                        "rechazoAnterior" to (actual.get("rechazo") ?: FieldValue.delete()),
                        "reenviado" to true,
                        "camposCorregidos" to camposCorregidos,
                        "enviadoEn" to FieldValue.serverTimestamp(),
                        "actualizadoEn" to FieldValue.serverTimestamp(),
                    ),
                )
                true
            }.await()
        }

    suspend fun cambiarPausa(uid: String, pausado: Boolean) {
        llamarFirebaseRestaurante {
            documento(uid).update(
                mapOf(
                    "pausado" to pausado,
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
