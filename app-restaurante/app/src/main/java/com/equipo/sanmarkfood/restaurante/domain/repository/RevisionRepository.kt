package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.admin.DetalleSolicitud
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Rechazo
import kotlinx.coroutines.flow.Flow

interface RevisionRepository {
    fun observar(uid: String): Flow<DetalleSolicitud?>

    suspend fun aprobar(uid: String)

    suspend fun rechazar(uid: String, rechazo: Rechazo)
}
