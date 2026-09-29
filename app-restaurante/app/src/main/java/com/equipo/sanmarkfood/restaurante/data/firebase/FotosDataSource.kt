package com.equipo.sanmarkfood.restaurante.data.firebase

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

// Fotos del local en Storage: restaurantes/{uid}/portada-<hora>.jpg y logo-<hora>.jpg.
// Cada subida tiene un nombre nuevo: al sobrescribir un archivo, Storage cambia su URL de descarga y
// la que ya estaba guardada en Firestore dejaría de funcionar hasta volver a guardar.
class FotosDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {
    private fun carpeta(uid: String) = storage.reference.child("restaurantes").child(uid)

    suspend fun subir(uid: String, nombre: String, jpeg: ByteArray, alAvanzar: (Float) -> Unit): String =
        llamarFirebaseRestaurante {
            val referencia = carpeta(uid).child(nombre)
            val metadatos = StorageMetadata.Builder().setContentType("image/jpeg").build()
            val tarea = referencia.putBytes(jpeg, metadatos)
            tarea.addOnProgressListener { avance ->
                if (avance.totalByteCount > 0) alAvanzar(avance.bytesTransferred.toFloat() / avance.totalByteCount)
            }
            try {
                tarea.await()
            } catch (e: CancellationException) {
                tarea.cancel()
                throw e
            }
            referencia.downloadUrl.await().toString()
        }

    /** Borra de la carpeta del local los archivos que no están en [enUso] (nombres de archivo). */
    suspend fun borrarLasDemas(uid: String, enUso: Set<String>) {
        llamarFirebaseRestaurante {
            carpeta(uid).listAll().await().items
                .filter { it.name !in enUso }
                .forEach { it.delete().await() }
        }
    }

    fun nombreDeArchivo(url: String): String = storage.getReferenceFromUrl(url).name
}
