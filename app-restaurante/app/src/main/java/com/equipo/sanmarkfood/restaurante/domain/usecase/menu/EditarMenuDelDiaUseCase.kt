package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.MenuDelDia
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OpcionMenu
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import javax.inject.Inject

class EditarMenuDelDiaUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(
        menu: MenuDelDia,
        precio: String,
        entradas: List<String>,
        segundos: List<String>,
        refresco: String,
        postre: String,
        horaFin: String,
    ) {
        val nuevos = validarMenu(precio, entradas, segundos, refresco, postre, horaFin)
        val datos = nuevos.copy(
            entradas = conservarAgotados(nuevos.entradas, menu.datos.entradas),
            segundos = conservarAgotados(nuevos.segundos, menu.datos.segundos),
        )
        menuRepository.actualizarMenu(menu.fecha, datos, menu.estado)
    }
}

internal fun conservarAgotados(nuevas: List<OpcionMenu>, anteriores: List<OpcionMenu>): List<OpcionMenu> {
    val agotadas = anteriores.filter { it.agotado }.map { it.nombre }
    return nuevas.map { it.copy(agotado = it.nombre in agotadas) }
}
