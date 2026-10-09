package com.equipo.sanmarkfood.comensal.domain.repository

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import kotlinx.coroutines.flow.Flow

interface RestauranteRepository {
    /** Los locales con estado "aprobado". Los fallos siempre llevan un ErrorDescubrimiento. */
    suspend fun obtenerRestaurantesAprobados(): Result<List<Restaurante>>

    /**
     * Los locales con estado "aprobado", en vivo: emite la lista de nuevo cada vez que
     * cambia en Firestore. Si falla, el flujo termina con un ErrorDescubrimiento.
     */
    fun observarRestaurantesAprobados(): Flow<List<Restaurante>>
}