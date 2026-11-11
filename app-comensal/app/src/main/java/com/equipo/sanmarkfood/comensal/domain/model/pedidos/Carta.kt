package com.equipo.sanmarkfood.comensal.domain.model.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.fechaDeHoy

data class CategoriaCarta(
    val id: String,
    val nombre: String,
    val orden: Int,
)

data class Plato(
    val id: String,
    val nombre: String,
    val descripcion: String?,
    val precio: Int,
    val categoriaId: String,
    val fotoUrl: String?,
    val agotadoEl: String?,
) {
    val agotadoHoy: Boolean get() = agotadoEl == fechaDeHoy()
}

data class SeccionCarta(
    val categoria: CategoriaCarta,
    val platos: List<Plato>,
)