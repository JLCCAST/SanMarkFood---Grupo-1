package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.MenuDelDia
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import javax.inject.Inject

class CambiarHoraFinUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(menu: MenuDelDia, horaFin: String) =
        menuRepository.actualizarMenu(menu.fecha, menu.datos.copy(horaFin = horaFin), menu.estado)
}
