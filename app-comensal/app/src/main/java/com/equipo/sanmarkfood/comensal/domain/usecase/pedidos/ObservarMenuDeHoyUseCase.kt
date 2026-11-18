package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.fechaDeHoy
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MenuDeHoy
import com.equipo.sanmarkfood.comensal.domain.repository.CartaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservarMenuDeHoyUseCase @Inject constructor(
    private val cartaRepository: CartaRepository,
) {
    operator fun invoke(restauranteId: String): Flow<MenuDeHoy?> =
        cartaRepository.observarMenu(restauranteId, fechaDeHoy())
}
