package com.equipo.sanmarkfood.comensal.domain.repository

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante

interface RestauranteRepository {
    /** Los locales con estado "aprobado". Los fallos siempre llevan un ErrorDescubrimiento. */
    suspend fun obtenerRestaurantesAprobados(): Result<List<Restaurante>>
}