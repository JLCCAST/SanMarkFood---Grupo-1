package com.equipo.sanmarkfood.comensal.domain.model.descubrimiento

data class FiltroDescubrimiento(
    val categoria: CategoriaRestaurante? = null,
    val precioMinimo: Int? = null,
    val precioMaximo: Int? = null,
    val calificacionMinima: Double? = null,
    val soloConMenuHoy: Boolean = false,
    val soloAbiertoAhora: Boolean = false,
    val soloFavoritos: Boolean = false
)