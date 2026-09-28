package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.firebase.AuthDataSource
import com.equipo.sanmarkfood.restaurante.data.firebase.RestaurantesDataSource
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorAuth
import com.equipo.sanmarkfood.restaurante.domain.repository.RestauranteRepository
import javax.inject.Inject

class RestauranteRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val restaurantesDataSource: RestaurantesDataSource,
) : RestauranteRepository {

    override suspend fun tieneLocal(): Boolean = restaurantesDataSource.existe(uid())

    // Cada cuenta tiene un solo local, guardado en restaurantes/{uid}.
    private fun uid(): String = authDataSource.usuarioActual()?.uid ?: throw ErrorAuth.Desconocido
}
