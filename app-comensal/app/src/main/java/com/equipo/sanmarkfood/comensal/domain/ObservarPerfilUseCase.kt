package com.equipo.sanmarkfood.comensal.domain

import com.equipo.sanmarkfood.comensal.data.Comensal
import com.equipo.sanmarkfood.comensal.data.ComensalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Escucha el perfil del comensal en tiempo real. */
class ObservarPerfilUseCase @Inject constructor(
    private val repository: ComensalRepository
) {
    operator fun invoke(uid: String): Flow<Comensal> = repository.observarPerfil(uid)
}