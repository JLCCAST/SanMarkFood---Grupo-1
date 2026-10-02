package com.equipo.sanmarkfood.comensal.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ComensalRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun documento(uid: String) = firestore.collection("usuarios").document(uid)


    private suspend fun guardar(uid: String, datos: Map<String, Any>) {
        documento(uid).set(datos + ("rol" to "comensal"), SetOptions.merge()).await()
    }

    suspend fun obtenerPerfil(uid: String): Result<Comensal> =
        runCatching {
            documento(uid).get().await().toObject(Comensal::class.java)
                ?: Comensal(uid = uid)
        }

    suspend fun actualizarPerfil(
        uid: String,
        nombre: String,
        telefono: String
    ): Result<Unit> =
        runCatching {
            guardar(uid, mapOf("nombre" to nombre, "telefono" to telefono))
        }

    suspend fun actualizarDirecciones(uid: String, direcciones: List<Direccion>): Result<Unit> =
        runCatching {
            guardar(uid, mapOf("direcciones" to direcciones))
        }

    suspend fun actualizarPreferenciasNotificacion(
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

    suspend fun subirFoto(uid: String, uri: Uri): Result<String> =
        runCatching {
            auth.currentUser?.getIdToken(true)?.await()

            val referencia = storage.reference.child("comensales/$uid/foto.jpg")
            referencia.putFile(uri).await()
            referencia.downloadUrl.await().toString()
        }

    suspend fun actualizarFoto(uid: String, fotoUrl: String): Result<Unit> =
        runCatching {
            guardar(uid, mapOf("fotoUrl" to fotoUrl))
        }
}