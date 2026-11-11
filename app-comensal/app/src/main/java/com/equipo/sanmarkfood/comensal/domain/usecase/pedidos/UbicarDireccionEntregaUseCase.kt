package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Coordenadas
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.DireccionEntrega
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorUbicacion
import com.equipo.sanmarkfood.comensal.domain.model.perfil.Direccion
import com.equipo.sanmarkfood.comensal.domain.repository.GeocodificacionRepository
import javax.inject.Inject

class UbicarDireccionEntregaUseCase @Inject constructor(
    private val geocodificacion: GeocodificacionRepository,
) {
    suspend operator fun invoke(direccion: Direccion): DireccionEntrega {
        if (direccion.direccionTexto.isBlank()) throw ErrorUbicacion.NoEncontrada
        val coordenadas = if (direccion.latitud != 0.0 || direccion.longitud != 0.0) {
            Coordenadas(direccion.latitud, direccion.longitud)
        } else {
            geocodificacion.coordenadasDe(direccion.direccionTexto)
        }
        val texto = listOf(direccion.etiqueta, direccion.direccionTexto)
            .filter { it.isNotBlank() }
            .joinToString(" · ")
            .take(MAX_TEXTO)
        return DireccionEntrega(texto = texto, latitud = coordenadas.latitud, longitud = coordenadas.longitud)
    }

    private companion object {
        const val MAX_TEXTO = 200
    }
}
