package com.equipo.sanmarkfood.comensal.domain.repository

import kotlinx.coroutines.flow.Flow

/** Locales que el comensal marcó como favoritos; se guardan en su documento `usuarios/{uid}`. */
interface FavoritosRepository {

    /** Ids de los locales favoritos; se actualiza solo cuando cambian en Firestore. */
    fun observarFavoritos(): Flow<Set<String>>

    suspend fun marcarFavorito(restauranteId: String): Result<Unit>

    suspend fun quitarFavorito(restauranteId: String): Result<Unit>
}