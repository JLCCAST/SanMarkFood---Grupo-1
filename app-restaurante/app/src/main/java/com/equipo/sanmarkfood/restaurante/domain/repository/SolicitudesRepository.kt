package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.admin.SolicitudLocal
import kotlinx.coroutines.flow.Flow

interface SolicitudesRepository {
    fun observarPendientes(): Flow<List<SolicitudLocal>>
}
