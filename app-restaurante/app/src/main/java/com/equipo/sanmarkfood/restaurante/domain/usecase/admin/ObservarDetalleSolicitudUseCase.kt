package com.equipo.sanmarkfood.restaurante.domain.usecase.admin

import com.equipo.sanmarkfood.restaurante.domain.model.admin.DetalleSolicitud
import com.equipo.sanmarkfood.restaurante.domain.repository.RevisionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservarDetalleSolicitudUseCase @Inject constructor(
    private val revisionRepository: RevisionRepository
) {
    operator fun invoke(uid: String): Flow<DetalleSolicitud?> = revisionRepository.observar(uid)
}
