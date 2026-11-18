package com.equipo.sanmarkfood.comensal.domain.repository

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Coordenadas

interface GeocodificacionRepository {
    suspend fun coordenadasDe(direccion: String): Coordenadas
}
