package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.admin.SolicitudLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import kotlinx.coroutines.flow.Flow

interface SolicitudesRepository {
    fun observar(estado: EstadoRestaurante): Flow<List<SolicitudLocal>>
}
