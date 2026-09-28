package com.equipo.sanmarkfood.restaurante.domain.usecase

import com.equipo.sanmarkfood.restaurante.domain.model.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Ubicacion
import com.equipo.sanmarkfood.restaurante.domain.repository.RestauranteRepository
import javax.inject.Inject

class GuardarDatosLocalUseCase @Inject constructor(
    private val restauranteRepository: RestauranteRepository
) {
    suspend operator fun invoke(
        nombre: String,
        categoria: CategoriaRestaurante?,
        direccion: String,
        ubicacion: Ubicacion,
        telefono: String,
        portadaUrl: String?,
        logoUrl: String?,
    ) {
        restauranteRepository.guardarDatos(
            validarDatosLocal(nombre, categoria, direccion, ubicacion, telefono, portadaUrl, logoUrl)
        )
    }
}
