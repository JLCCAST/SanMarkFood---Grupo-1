package com.equipo.sanmarkfood.comensal.domain.model.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.EstadoMenuDelDia

data class OpcionMenu(
    val nombre: String,
    val agotado: Boolean,
)

data class MenuDeHoy(
    val fecha: String,
    val precio: Int,
    val entradas: List<OpcionMenu>,
    val segundos: List<OpcionMenu>,
    val refresco: String?,
    val postre: String?,
    val horaFin: String,
    val estado: EstadoMenuDelDia,
) {
    fun armar(entrada: String, segundo: String): MenuDelCarrito? {
        if (entradas.none { it.nombre == entrada && !it.agotado }) return null
        if (segundos.none { it.nombre == segundo && !it.agotado }) return null
        return MenuDelCarrito(
            fecha = fecha,
            entrada = entrada,
            segundo = segundo,
            precio = precio,
            cantidad = 1,
        )
    }
}
