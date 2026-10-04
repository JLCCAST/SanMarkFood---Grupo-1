package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import javax.inject.Inject

class CrearCategoriaUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(nombre: String): Categoria {
        val limpio = nombre.trim()
        if (limpio.isEmpty() || limpio.length > Categoria.MAX_NOMBRE) throw ErrorMenu.NombreCategoriaInvalido

        val existentes = menuRepository.leerCategorias()
        if (existentes.any { it.nombre.equals(limpio, ignoreCase = true) }) throw ErrorMenu.CategoriaRepetida

        val orden = (existentes.maxOfOrNull { it.orden } ?: -1) + 1
        return menuRepository.crearCategoria(limpio, orden)
    }
}
