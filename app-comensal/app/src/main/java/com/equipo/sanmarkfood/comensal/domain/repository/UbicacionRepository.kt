package com.equipo.sanmarkfood.comensal.domain.repository

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Coordenadas

interface UbicacionRepository {
    /** Devuelve null si falta el permiso o no se pudo obtener la ubicación. */
    suspend fun obtenerUbicacionActual(): Coordenadas?
}