package com.equipo.sanmarkfood.comensal.data

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ComensalRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private fun documento(uid: String) = firestore.collection("usuarios").document(uid)

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
            documento(uid).set(
                mapOf("nombre" to nombre, "telefono" to telefono),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        }

    suspend fun actualizarDirecciones(uid: String, direcciones: List<Direccion>): Result<Unit> =
        runCatching {
            documento(uid).set(
                mapOf("direcciones" to direcciones),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        }

    suspend fun actualizarPreferenciasNotificacion(
        uid: String,
        notificarReservas: Boolean,
        notificarResenas: Boolean
    ): Result<Unit> =
        runCatching {
            documento(uid).set(
                mapOf(
                    "notificarReservas" to notificarReservas,
                    "notificarResenas" to notificarResenas
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        }

    suspend fun subirFoto(uid: String, uri: Uri): Result<String> =
        runCatching {
            val referencia = storage.reference.child("comensales/$uid/foto.jpg")
            referencia.putFile(uri).await()
            referencia.downloadUrl.await().toString()
        }

    suspend fun actualizarFoto(uid: String, fotoUrl: String): Result<Unit> =
        runCatching {
            documento(uid).set(
                mapOf("fotoUrl" to fotoUrl),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        }
}