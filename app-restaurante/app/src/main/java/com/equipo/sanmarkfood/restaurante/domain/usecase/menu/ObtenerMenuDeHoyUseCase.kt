package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.MenuDelDia
import com.equipo.sanmarkfood.restaurante.domain.model.menu.fechaDeHoy
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import javax.inject.Inject

class ObtenerMenuDeHoyUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(): MenuDelDia? = menuRepository.leerMenu(fechaDeHoy())
}
