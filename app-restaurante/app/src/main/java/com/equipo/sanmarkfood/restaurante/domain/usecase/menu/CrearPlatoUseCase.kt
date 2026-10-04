package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.CampoPlato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import javax.inject.Inject

class CrearPlatoUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(
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
        menuRepository.crearPlato(datos, fotoLocal)
    }
}
