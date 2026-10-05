package com.equipo.sanmarkfood.restaurante.domain.usecase.admin

import com.equipo.sanmarkfood.restaurante.domain.repository.RevisionRepository
import javax.inject.Inject

class AprobarLocalUseCase @Inject constructor(
    private val revisionRepository: RevisionRepository
) {
    suspend operator fun invoke(uid: String) = revisionRepository.aprobar(uid)
}
