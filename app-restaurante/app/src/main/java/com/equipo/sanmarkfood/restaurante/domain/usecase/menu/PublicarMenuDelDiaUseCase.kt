package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.OrigenMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.fechaDeHoy
import com.equipo.sanmarkfood.restaurante.domain.repository.BorradorMenuRepository
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import javax.inject.Inject

class PublicarMenuDelDiaUseCase @Inject constructor(
    private val menuRepository: MenuRepository,
    private val borradorMenuRepository: BorradorMenuRepository,
) {
    suspend operator fun invoke(
        precio: String,
        entradas: List<String>,
        segundos: List<String>,
        refresco: String,
        postre: String,
        horaFin: String,
        origen: OrigenMenu,
    ) {
        val datos = validarMenu(precio, entradas, segundos, refresco, postre, horaFin)
        menuRepository.publicarMenu(fechaDeHoy(), datos, origen)
        borradorMenuRepository.borrarBorrador()
    }
}
