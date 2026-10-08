package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.OrigenMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.fechaDeHoy
import com.equipo.sanmarkfood.restaurante.domain.repository.BorradorMenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObservarPizarraLeidaUseCase @Inject constructor(
    private val borradorMenuRepository: BorradorMenuRepository
) {
    operator fun invoke(): Flow<Boolean> =
        borradorMenuRepository.observarOrigen(fechaDeHoy()).map { it == OrigenMenu.IA }
}
