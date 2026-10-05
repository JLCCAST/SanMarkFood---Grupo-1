package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.Plato
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import javax.inject.Inject

class EliminarPlatoUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(plato: Plato) = menuRepository.eliminarPlato(plato)
}
