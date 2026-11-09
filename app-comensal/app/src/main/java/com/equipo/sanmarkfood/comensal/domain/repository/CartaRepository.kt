package com.equipo.sanmarkfood.comensal.domain.repository

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.CategoriaCarta
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Plato
import kotlinx.coroutines.flow.Flow

interface CartaRepository {
    fun observarCategorias(restauranteId: String): Flow<List<CategoriaCarta>>
    fun observarPlatos(restauranteId: String): Flow<List<Plato>>
}