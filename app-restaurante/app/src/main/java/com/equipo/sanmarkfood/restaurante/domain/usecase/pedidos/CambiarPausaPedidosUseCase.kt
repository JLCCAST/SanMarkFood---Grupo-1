package com.equipo.sanmarkfood.restaurante.domain.usecase.pedidos

import com.equipo.sanmarkfood.restaurante.domain.repository.RestauranteRepository
import javax.inject.Inject

class CambiarPausaPedidosUseCase @Inject constructor(
    private val restauranteRepository: RestauranteRepository
) {
    suspend operator fun invoke(pausado: Boolean) = restauranteRepository.cambiarPausa(pausado)
}
