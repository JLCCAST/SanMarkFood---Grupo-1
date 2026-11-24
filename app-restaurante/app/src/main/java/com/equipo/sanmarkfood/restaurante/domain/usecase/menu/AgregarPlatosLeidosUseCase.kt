package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.PlatoLeido
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AgregarPlatosLeidosUseCase @Inject constructor(
    private val menuRepository: MenuRepository,
) {
    suspend operator fun invoke(platos: List<PlatoLeido>): Int {
        val invalidos = platos.indices.filterNot { esValido(platos[it]) }.toSet()
        if (invalidos.isNotEmpty()) throw ErrorMenu.PlatosLeidosInvalidos(invalidos)

        val categorias = menuRepository.leerCategorias().toMutableList()
        val existentes = menuRepository.observarPlatos().first()
            .map { clave(it.datos.categoriaId, it.datos.nombre) }
            .toMutableSet()

        var agregados = 0
        for (plato in platos) {
            val categoria = categoriaPara(plato.categoria, categorias)
            if (!existentes.add(clave(categoria.id, plato.nombre))) continue
            menuRepository.crearPlato(validarPlato(plato.nombre, "", plato.precio, categoria.id, true), null)
            agregados++
        }
        return agregados
    }

    private suspend fun categoriaPara(nombre: String, categorias: MutableList<Categoria>): Categoria {
        val limpio = nombre.trim().take(Categoria.MAX_NOMBRE).trim().ifEmpty { PlatoLeido.CATEGORIA_POR_DEFECTO }
        categorias.find { it.nombre.equals(limpio, ignoreCase = true) }?.let { return it }
        val orden = (categorias.maxOfOrNull { it.orden } ?: -1) + 1
        return menuRepository.crearCategoria(limpio, orden).also { categorias += it }
    }

    private fun esValido(plato: PlatoLeido): Boolean = try {
        validarPlato(plato.nombre, "", plato.precio, plato.categoria.ifBlank { PlatoLeido.CATEGORIA_POR_DEFECTO }, true)
        true
    } catch (e: ErrorMenu.DatosPlatoInvalidos) {
        false
    }

    private fun clave(categoriaId: String, nombre: String): String = "$categoriaId|${nombre.trim().lowercase()}"
}
