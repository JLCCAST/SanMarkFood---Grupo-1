package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosPlato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.MenuDelDia
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OpcionMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OrigenMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.Plato
import kotlinx.coroutines.flow.Flow

interface MenuRepository {
    fun observarCategorias(): Flow<List<Categoria>>

    suspend fun leerCategorias(): List<Categoria>

    suspend fun crearCategoria(nombre: String, orden: Int): Categoria

    fun observarPlatos(): Flow<List<Plato>>

    suspend fun obtenerPlato(id: String): Plato?

    suspend fun crearPlato(datos: DatosPlato, fotoLocal: String?)

    suspend fun actualizarPlato(plato: Plato, datos: DatosPlato, fotoLocal: String?)

    suspend fun eliminarPlato(plato: Plato)

    suspend fun cambiarAgotadoPlato(platoId: String, agotadoEl: String?)

    fun observarMenu(fecha: String): Flow<MenuDelDia?>

    suspend fun publicarMenu(fecha: String, datos: DatosMenu, origen: OrigenMenu)

    suspend fun guardarOpcionesMenu(fecha: String, entradas: List<OpcionMenu>, segundos: List<OpcionMenu>)
}
