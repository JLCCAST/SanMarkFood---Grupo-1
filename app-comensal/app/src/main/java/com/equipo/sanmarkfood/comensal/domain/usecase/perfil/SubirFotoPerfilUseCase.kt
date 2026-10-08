package com.equipo.sanmarkfood.comensal.domain.usecase.perfil

import com.equipo.sanmarkfood.comensal.domain.repository.ComensalRepository
import javax.inject.Inject

/** Sube la imagen a Storage y, si salió bien, guarda su enlace en el perfil. */
class SubirFotoPerfilUseCase @Inject constructor(
    private val repository: ComensalRepository
) {
    suspend operator fun invoke(uid: String, rutaLocal: String): Result<Unit> =
        repository.subirFoto(uid, rutaLocal).fold(
            onSuccess = { url -> repository.actualizarFoto(uid, url) },
            onFailure = { Result.failure(it) }
        )
}