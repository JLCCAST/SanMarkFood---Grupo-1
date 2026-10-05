package com.equipo.sanmarkfood.restaurante.domain.usecase.gestion_restaurante

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Ubicacion
import com.equipo.sanmarkfood.restaurante.domain.repository.RestauranteRepository
import com.equipo.sanmarkfood.restaurante.domain.repository.VerificacionTelefonoRepository
import javax.inject.Inject

class GuardarDatosLocalUseCase @Inject constructor(
    private val restauranteRepository: RestauranteRepository,
    private val verificacionTelefonoRepository: VerificacionTelefonoRepository,
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
        val datos = validarDatosLocal(nombre, categoria, direccion, ubicacion, telefono, portadaUrl, logoUrl)
        if (verificacionTelefonoRepository.telefonoVerificado() != datos.telefono) {
            throw ErrorRestaurante.TelefonoSinVerificar
        }
        if (restauranteRepository.obtener()?.requiereNuevaRevision(datos) == true) {
            restauranteRepository.guardarDatosYPedirRevision(datos)
        } else {
            restauranteRepository.guardarDatos(datos)
        }
    }
}
