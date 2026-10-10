package com.equipo.sanmarkfood.comensal.domain.usecase.descubrimiento

import com.equipo.sanmarkfood.comensal.domain.repository.FavoritosRepository
import javax.inject.Inject

/** Marca un local como favorito, o lo quita si ya lo era. */
class AlternarFavoritoUseCase @Inject constructor(
    private val favoritosRepository: FavoritosRepository
) {
    suspend operator fun invoke(restauranteId: String, eraFavorito: Boolean): Result<Unit> =
        if (eraFavorito) {
            favoritosRepository.quitarFavorito(restauranteId)
        } else {
            favoritosRepository.marcarFavorito(restauranteId)
        }
}