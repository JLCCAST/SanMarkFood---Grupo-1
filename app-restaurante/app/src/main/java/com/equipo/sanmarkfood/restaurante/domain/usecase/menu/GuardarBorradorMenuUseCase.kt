package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.BorradorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.fechaDeHoy
import com.equipo.sanmarkfood.restaurante.domain.repository.BorradorMenuRepository
import javax.inject.Inject

class GuardarBorradorMenuUseCase @Inject constructor(
    private val borradorMenuRepository: BorradorMenuRepository
) {
    suspend operator fun invoke(borrador: BorradorMenu) =
        borradorMenuRepository.guardarBorrador(fechaDeHoy(), borrador)
}
