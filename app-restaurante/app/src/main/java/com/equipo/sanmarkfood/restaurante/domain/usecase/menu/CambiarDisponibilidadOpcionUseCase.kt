package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.MenuDelDia
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OpcionMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.TipoOpcion
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import javax.inject.Inject

class CambiarDisponibilidadOpcionUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(menu: MenuDelDia, tipo: TipoOpcion, indice: Int, disponible: Boolean) {
        val datos = menu.datos
        val entradas = if (tipo == TipoOpcion.ENTRADA) cambiar(datos.entradas, indice, disponible) else datos.entradas
        val segundos = if (tipo == TipoOpcion.SEGUNDO) cambiar(datos.segundos, indice, disponible) else datos.segundos
        menuRepository.guardarOpcionesMenu(menu.fecha, entradas, segundos)
    }

    private fun cambiar(opciones: List<OpcionMenu>, indice: Int, disponible: Boolean): List<OpcionMenu> =
        opciones.mapIndexed { i, opcion -> if (i == indice) opcion.copy(agotado = !disponible) else opcion }
}
