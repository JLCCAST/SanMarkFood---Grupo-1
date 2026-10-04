package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosPlato
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
}
