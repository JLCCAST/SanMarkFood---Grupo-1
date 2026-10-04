package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.Plato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.fechaDeHoy
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import javax.inject.Inject

class CambiarDisponibilidadPlatoUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(plato: Plato, disponible: Boolean) {
        val agotadoEl = if (disponible) null else fechaDeHoy()
        menuRepository.cambiarAgotadoPlato(plato.id, agotadoEl)
    }
}
