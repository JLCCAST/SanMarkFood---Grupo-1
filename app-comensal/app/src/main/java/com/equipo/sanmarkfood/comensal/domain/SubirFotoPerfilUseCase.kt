package com.equipo.sanmarkfood.comensal.domain

import android.net.Uri
import com.equipo.sanmarkfood.comensal.data.ComensalRepository
import javax.inject.Inject

/** Sube la imagen a Storage y, si salió bien, guarda su enlace en el perfil. */
class SubirFotoPerfilUseCase @Inject constructor(
    private val repository: ComensalRepository
) {
    suspend operator fun invoke(uid: String, uri: Uri): Result<Unit> =
        repository.subirFoto(uid, uri).fold(
            onSuccess = { url -> repository.actualizarFoto(uid, url) },
            onFailure = { Result.failure(it) }
        )
}