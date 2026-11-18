package com.equipo.sanmarkfood.comensal.domain.usecase.descubrimiento

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import com.equipo.sanmarkfood.comensal.domain.repository.RestauranteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObservarRestauranteUseCase @Inject constructor(
    private val restauranteRepository: RestauranteRepository,
) {
    operator fun invoke(restauranteId: String): Flow<Restaurante?> =
        restauranteRepository.observarRestaurantesAprobados()
            .map { restaurantes -> restaurantes.firstOrNull { it.id == restauranteId } }
            .distinctUntilChanged()
}
