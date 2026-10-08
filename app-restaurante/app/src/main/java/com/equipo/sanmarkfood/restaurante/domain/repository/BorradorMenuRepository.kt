package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.menu.BorradorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OrigenMenu
import kotlinx.coroutines.flow.Flow

interface BorradorMenuRepository {
    suspend fun leerBorrador(fecha: String): BorradorMenu?

    fun observarOrigen(fecha: String): Flow<OrigenMenu?>

    suspend fun guardarBorrador(fecha: String, borrador: BorradorMenu)

    suspend fun borrarBorrador()
}
