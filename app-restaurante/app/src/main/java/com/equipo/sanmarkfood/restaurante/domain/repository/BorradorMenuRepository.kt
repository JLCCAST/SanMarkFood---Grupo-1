package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.menu.BorradorMenu

interface BorradorMenuRepository {
    suspend fun leerBorrador(fecha: String): BorradorMenu?

    suspend fun guardarBorrador(fecha: String, borrador: BorradorMenu)

    suspend fun borrarBorrador()
}
