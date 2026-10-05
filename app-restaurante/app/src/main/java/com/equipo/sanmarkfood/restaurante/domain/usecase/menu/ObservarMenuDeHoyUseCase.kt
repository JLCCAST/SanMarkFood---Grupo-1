package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.MenuDelDia
import com.equipo.sanmarkfood.restaurante.domain.model.menu.fechaDeHoy
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservarMenuDeHoyUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    operator fun invoke(): Flow<MenuDelDia?> = menuRepository.observarMenu(fechaDeHoy())
}
