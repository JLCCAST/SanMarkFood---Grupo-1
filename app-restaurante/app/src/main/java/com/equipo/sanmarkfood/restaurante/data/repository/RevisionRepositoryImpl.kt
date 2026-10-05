package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.firebase.RevisionDataSource
import com.equipo.sanmarkfood.restaurante.domain.model.admin.DetalleSolicitud
import com.equipo.sanmarkfood.restaurante.domain.model.admin.ErrorAdmin
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Rechazo
import com.equipo.sanmarkfood.restaurante.domain.repository.RevisionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RevisionRepositoryImpl @Inject constructor(
    private val revisionDataSource: RevisionDataSource
) : RevisionRepository {

    override fun observar(uid: String): Flow<DetalleSolicitud?> = revisionDataSource.observar(uid)

    override suspend fun aprobar(uid: String) {
        if (!revisionDataSource.aprobar(uid)) throw ErrorAdmin.YaRevisada
    }

    override suspend fun rechazar(uid: String, rechazo: Rechazo) {
        if (!revisionDataSource.rechazar(uid, rechazo)) throw ErrorAdmin.YaRevisada
    }
}
