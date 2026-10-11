package com.equipo.sanmarkfood.comensal.domain.usecase.descubrimiento

import com.equipo.sanmarkfood.comensal.domain.repository.FavoritosRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Ids de los locales favoritos del comensal; se actualiza solo cuando cambian. */
class ObservarFavoritosUseCase @Inject constructor(
    private val favoritosRepository: FavoritosRepository
) {
    operator fun invoke(): Flow<Set<String>> = favoritosRepository.observarFavoritos()
}