package com.equipo.sanmarkfood.comensal.domain.model.pedidos

sealed interface ResultadoAgregarAlCarrito {
    data object Agregado : ResultadoAgregarAlCarrito

    data class OtroLocal(val restauranteActual: String) : ResultadoAgregarAlCarrito

    data object LimiteAlcanzado : ResultadoAgregarAlCarrito
}
