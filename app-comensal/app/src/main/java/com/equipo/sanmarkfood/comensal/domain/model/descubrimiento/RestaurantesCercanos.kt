package com.equipo.sanmarkfood.comensal.domain.model.descubrimiento

/**
 * La lista ya ordenada. [conDistancia] es false cuando no se pudo obtener la ubicación
 * del usuario: en ese caso la lista va ordenada por nombre y sin distancias.
 */
data class RestaurantesCercanos(
    val restaurantes: List<Restaurante>,
    val conDistancia: Boolean
)