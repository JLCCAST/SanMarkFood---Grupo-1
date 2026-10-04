package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.CampoPlato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.Plato
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import javax.inject.Inject

class EditarPlatoUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(
        plato: Plato,
        nombre: String,
        descripcion: String,
        precio: String,
        categoriaId: String?,
        fotoLocal: String?,
    ) {
        val datos = validarPlato(nombre, descripcion, precio, categoriaId)
        if (menuRepository.leerCategorias().none { it.id == datos.categoriaId }) {
            throw ErrorMenu.DatosPlatoInvalidos(setOf(CampoPlato.CATEGORIA))
        }
        menuRepository.actualizarPlato(plato, datos, fotoLocal)
    }
}
