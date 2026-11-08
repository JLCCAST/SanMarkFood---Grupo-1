package com.equipo.sanmarkfood.comensal.domain.model.pedidos

/** Un plato del carrito. `precio` va en céntimos y se copia al agregarlo. */
data class ItemCarrito(
    val platoId: String,
    val nombre: String,
    val precio: Int,
    val cantidad: Int,
    val fotoUrl: String? = null
)

/** Lo que el comensal va a pedir: platos de un solo local. Sin platos no hay carrito. */
data class Carrito(
    val restauranteId: String,
    val restauranteNombre: String,
    val items: List<ItemCarrito>
) {
    /** Total en céntimos. */
    val total: Int get() = items.sumOf { it.precio * it.cantidad }

    val cantidadTotal: Int get() = items.sumOf { it.cantidad }

    companion object {
        /** Los mismos topes que valida la función crearPedido. */
        const val MAX_CANTIDAD_POR_PLATO = 20
        const val MAX_PLATOS_DISTINTOS = 20
    }
}