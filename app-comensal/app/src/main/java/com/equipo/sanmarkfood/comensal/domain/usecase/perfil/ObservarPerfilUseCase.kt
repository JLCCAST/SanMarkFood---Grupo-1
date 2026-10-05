package com.equipo.sanmarkfood.comensal.domain.usecase.perfil

import com.equipo.sanmarkfood.comensal.domain.model.perfil.Comensal
import com.equipo.sanmarkfood.comensal.domain.repository.ComensalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Escucha el perfil del comensal en tiempo real. */
class ObservarPerfilUseCase @Inject constructor(
    private val repository: ComensalRepository
) {
    operator fun invoke(uid: String): Flow<Comensal> = repository.observarPerfil(uid)
}