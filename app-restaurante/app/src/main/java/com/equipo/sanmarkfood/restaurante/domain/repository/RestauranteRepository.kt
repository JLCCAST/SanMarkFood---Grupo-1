package com.equipo.sanmarkfood.restaurante.domain.repository

interface RestauranteRepository {
    suspend fun tieneLocal(): Boolean
}
