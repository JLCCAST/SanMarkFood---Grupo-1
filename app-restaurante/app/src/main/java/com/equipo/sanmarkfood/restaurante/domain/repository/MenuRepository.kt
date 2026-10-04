package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import kotlinx.coroutines.flow.Flow

interface MenuRepository {
    fun observarCategorias(): Flow<List<Categoria>>

    suspend fun leerCategorias(): List<Categoria>

    suspend fun crearCategoria(nombre: String, orden: Int): Categoria
}
