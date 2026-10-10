package com.equipo.sanmarkfood.comensal.data.repository

import com.equipo.sanmarkfood.comensal.domain.repository.FavoritosRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FavoritosRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : FavoritosRepository {

    private fun documento(uid: String) = firestore.collection("usuarios").document(uid)

    /** El uid de la cuenta activa; emite de nuevo cada vez que se inicia o se cierra sesión. */
    private fun uidActual(): Flow<String?> = callbackFlow<String?> {
        val oyente = FirebaseAuth.AuthStateListener { sesion ->
            trySend(sesion.currentUser?.uid)
        }
        auth.addAuthStateListener(oyente)
        awaitClose { auth.removeAuthStateListener(oyente) }
    }.distinctUntilChanged()

    /**
     * Los favoritos de la cuenta activa, en vivo. Si cambia la cuenta, se deja de escuchar
     * la anterior y se escucha la nueva; sin sesión (invitado) no hay favoritos.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observarFavoritos(): Flow<Set<String>> =
        uidActual().flatMapLatest { uid ->
            if (uid == null) flowOf(emptySet()) else favoritosDe(uid)
        }

    private fun favoritosDe(uid: String): Flow<Set<String>> = callbackFlow {
        val registro = documento(uid).addSnapshotListener { instantanea, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val ids = (instantanea?.get("favoritos") as? List<*>)
                ?.filterIsInstance<String>()
                ?.toSet()
                .orEmpty()
            trySend(ids)
        }
        awaitClose { registro.remove() }
    }

    override suspend fun marcarFavorito(restauranteId: String): Result<Unit> =
        modificar(FieldValue.arrayUnion(restauranteId))

    override suspend fun quitarFavorito(restauranteId: String): Result<Unit> =
        modificar(FieldValue.arrayRemove(restauranteId))

    private suspend fun modificar(cambio: FieldValue): Result<Unit> =
        runCatching {
            val uid = checkNotNull(auth.currentUser?.uid) { "No hay una sesión iniciada" }
            documento(uid)
                .set(mapOf("favoritos" to cambio, "rol" to "comensal"), SetOptions.merge())
                .await()
        }
}