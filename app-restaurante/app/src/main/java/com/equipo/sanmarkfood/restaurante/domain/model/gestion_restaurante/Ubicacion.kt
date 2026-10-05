package com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante

data class Ubicacion(val latitud: Double, val longitud: Double) {
    companion object {
        /** Punto de partida del mapa cuando el local todavía no marca su puerta. */
        val CiudadUniversitaria = Ubicacion(latitud = -12.0560, longitud = -77.0844)
    }
}
