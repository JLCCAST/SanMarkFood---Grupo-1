package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.menu.BorradorMenu

interface PizarraRepository {
    suspend fun leerMenu(foto: String): BorradorMenu
}
