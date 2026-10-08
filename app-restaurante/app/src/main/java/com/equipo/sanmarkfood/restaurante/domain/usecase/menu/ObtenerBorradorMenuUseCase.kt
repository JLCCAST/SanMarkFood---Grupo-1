package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.BorradorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.fechaDeHoy
import com.equipo.sanmarkfood.restaurante.domain.repository.BorradorMenuRepository
import javax.inject.Inject

class ObtenerBorradorMenuUseCase @Inject constructor(
    private val borradorMenuRepository: BorradorMenuRepository
) {
    suspend operator fun invoke(): BorradorMenu? = borradorMenuRepository.leerBorrador(fechaDeHoy())
}
