package com.equipo.sanmarkfood.comensal.domain.model.pedidos

/** Lo que puede pasar al agregar un plato; el ViewModel decide qué mostrar en cada caso. */
sealed interface ResultadoAgregarAlCarrito {
    /** El plato quedó en el carrito. */
    data object Agregado : ResultadoAgregarAlCarrito

    /** El carrito ya tiene platos de otro local: hay que preguntar si se vacía. */
    data class OtroLocal(val restauranteActual: String) : ResultadoAgregarAlCarrito

    /** El plato ya está en la cantidad máxima, o el carrito ya tiene el máximo de platos. */
    data object LimiteAlcanzado : ResultadoAgregarAlCarrito
}