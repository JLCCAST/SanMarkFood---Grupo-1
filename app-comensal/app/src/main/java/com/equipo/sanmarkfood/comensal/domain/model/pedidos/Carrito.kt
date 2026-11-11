package com.equipo.sanmarkfood.comensal.domain.model.pedidos

sealed interface ItemCarrito {
    val clave: String
    val precio: Int
    val cantidad: Int
    val subtotal: Int get() = precio * cantidad
}

data class PlatoDelCarrito(
    val platoId: String,
    val nombre: String,
    override val precio: Int,
    override val cantidad: Int,
    val fotoUrl: String? = null,
) : ItemCarrito {
    override val clave: String get() = claveDePlato(platoId)
}

data class MenuDelCarrito(
    val fecha: String,
    val entrada: String,
    val segundo: String,
    override val precio: Int,
    override val cantidad: Int,
) : ItemCarrito {
    override val clave: String get() = claveDeMenu(entrada, segundo)
}

fun claveDePlato(platoId: String): String = "plato:$platoId"

fun claveDeMenu(entrada: String, segundo: String): String = "menu:$entrada|$segundo"

data class Carrito(
    val restauranteId: String,
    val restauranteNombre: String,
    val items: List<ItemCarrito>,
) {
    val total: Int get() = items.sumOf { it.subtotal }

    val cantidadTotal: Int get() = items.sumOf { it.cantidad }

    val cantidadDeMenus: Int get() = items.filterIsInstance<MenuDelCarrito>().sumOf { it.cantidad }

    fun cantidadDe(clave: String): Int = items.firstOrNull { it.clave == clave }?.cantidad ?: 0

    companion object {
        const val MAX_CANTIDAD_POR_ITEM = 20
        const val MAX_ITEMS_DISTINTOS = 20
    }
}
