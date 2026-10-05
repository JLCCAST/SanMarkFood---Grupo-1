package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.firebase.SolicitudesDataSource
import com.equipo.sanmarkfood.restaurante.domain.model.admin.SolicitudLocal
import com.equipo.sanmarkfood.restaurante.domain.repository.SolicitudesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SolicitudesRepositoryImpl @Inject constructor(
    private val solicitudesDataSource: SolicitudesDataSource
) : SolicitudesRepository {

    override fun observarPendientes(): Flow<List<SolicitudLocal>> = solicitudesDataSource.observarPendientes()
}
