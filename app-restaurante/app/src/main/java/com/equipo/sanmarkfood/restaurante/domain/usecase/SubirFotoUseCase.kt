package com.equipo.sanmarkfood.restaurante.domain.usecase

import com.equipo.sanmarkfood.restaurante.domain.model.TipoFoto
import com.equipo.sanmarkfood.restaurante.domain.repository.RestauranteRepository
import javax.inject.Inject

class SubirFotoUseCase @Inject constructor(
    private val restauranteRepository: RestauranteRepository
) {
    suspend operator fun invoke(tipo: TipoFoto, imagenLocal: String, alAvanzar: (fraccion: Float) -> Unit): String =
        restauranteRepository.subirFoto(tipo, imagenLocal, alAvanzar)
}
