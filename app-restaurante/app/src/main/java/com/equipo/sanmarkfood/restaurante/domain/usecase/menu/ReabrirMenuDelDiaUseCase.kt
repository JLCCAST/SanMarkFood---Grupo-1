package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.EstadoMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.MenuDelDia
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import javax.inject.Inject

class ReabrirMenuDelDiaUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(menu: MenuDelDia) =
        menuRepository.actualizarMenu(menu.fecha, menu.datos, EstadoMenu.PUBLICADO)
}
