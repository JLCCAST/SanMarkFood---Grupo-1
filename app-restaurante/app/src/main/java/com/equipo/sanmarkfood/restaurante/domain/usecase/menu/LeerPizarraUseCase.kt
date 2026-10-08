package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.fechaDeHoy
import com.equipo.sanmarkfood.restaurante.domain.repository.BorradorMenuRepository
import com.equipo.sanmarkfood.restaurante.domain.repository.PizarraRepository
import javax.inject.Inject

class LeerPizarraUseCase @Inject constructor(
    private val pizarraRepository: PizarraRepository,
    private val borradorMenuRepository: BorradorMenuRepository,
) {
    suspend operator fun invoke(foto: String) {
        val borrador = pizarraRepository.leerMenu(foto)
        if (borrador.entradas.isEmpty() && borrador.segundos.isEmpty()) throw ErrorMenu.PizarraSinMenu
        borradorMenuRepository.guardarBorrador(fechaDeHoy(), borrador)
    }
}
