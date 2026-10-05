package com.equipo.sanmarkfood.restaurante.domain.usecase.gestion_restaurante

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Ubicacion
import com.equipo.sanmarkfood.restaurante.domain.repository.RestauranteRepository
import javax.inject.Inject

class RequiereNuevaRevisionUseCase @Inject constructor(
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
    ): Boolean {
        val datos = validarDatosLocal(nombre, categoria, direccion, ubicacion, telefono, portadaUrl, logoUrl)
        return restauranteRepository.obtener()?.requiereNuevaRevision(datos) == true
    }
}
