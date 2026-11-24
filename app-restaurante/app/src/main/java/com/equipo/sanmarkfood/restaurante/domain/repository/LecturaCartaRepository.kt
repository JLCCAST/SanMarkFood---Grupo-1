package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.menu.PlatoLeido

interface LecturaCartaRepository {
    suspend fun leerCarta(foto: String): List<PlatoLeido>

    fun ultimaLectura(): List<PlatoLeido>
}
