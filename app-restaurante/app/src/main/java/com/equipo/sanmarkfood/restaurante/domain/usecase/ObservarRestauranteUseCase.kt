package com.equipo.sanmarkfood.restaurante.domain.usecase

import com.equipo.sanmarkfood.restaurante.domain.model.Restaurante
import com.equipo.sanmarkfood.restaurante.domain.repository.RestauranteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservarRestauranteUseCase @Inject constructor(
    private val restauranteRepository: RestauranteRepository
) {
    operator fun invoke(): Flow<Restaurante?> = restauranteRepository.observar()
}
