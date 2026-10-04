package com.equipo.sanmarkfood.comensal.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ComensalRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : ComensalRepository {

    private fun documento(uid: String) = firestore.collection("usuarios").document(uid)

    // Las reglas de Firestore exigen "rol" cuando el documento se crea. Si ya existe,
    // el valor es el mismo y no cambia.
    private suspend fun guardar(uid: String, datos: Map<String, Any>) {
        documento(uid).set(datos + ("rol" to "comensal"), SetOptions.merge()).await()
    }

    override suspend fun obtenerPerfil(uid: String): Result<Comensal> =
        runCatching {
            documento(uid).get().await().toObject(Comensal::class.java)
                ?: Comensal(uid = uid)
        }

    override fun observarPerfil(uid: String): Flow<Comensal> = callbackFlow {
        val listener = documento(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val comensal = snapshot?.toObject(Comensal::class.java) ?: Comensal(uid = uid)
            trySend(comensal)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun actualizarPerfil(
        uid: String,
        nombre: String,
        telefono: String
    ): Result<Unit> =
        runCatching {
            guardar(uid, mapOf("nombre" to nombre, "telefono" to telefono))
        }

    override suspend fun actualizarDirecciones(
        uid: String,
        direcciones: List<Direccion>
    ): Result<Unit> =
        runCatching {
            guardar(uid, mapOf("direcciones" to direcciones))
        }

    override suspend fun actualizarPreferenciasNotificacion(
        uid: String,
        notificarReservas: Boolean,
        notificarResenas: Boolean
    ): Result<Unit> =
        runCatching {
            guardar(
                uid,
                mapOf(
                    "notificarReservas" to notificarReservas,
                    "notificarResenas" to notificarResenas
                )
            )
        }

    override suspend fun subirFoto(uid: String, uri: Uri): Result<String> =
        runCatching {
            // Las reglas de Storage leen email_verified del token de sesión, que se guarda
            // en caché; pedimos uno nuevo antes de subir.
            auth.currentUser?.getIdToken(true)?.await()

            val referencia = storage.reference.child("comensales/$uid/foto.jpg")
            referencia.putFile(uri).await()
            referencia.downloadUrl.await().toString()
        }

    override suspend fun actualizarFoto(uid: String, fotoUrl: String): Result<Unit> =
        runCatching {
            guardar(uid, mapOf("fotoUrl" to fotoUrl))
        }
}