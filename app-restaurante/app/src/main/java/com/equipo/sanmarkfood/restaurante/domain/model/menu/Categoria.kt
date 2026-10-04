package com.equipo.sanmarkfood.restaurante.domain.model.menu

data class Categoria(
    val id: String,
    val nombre: String,
    val orden: Int,
) {
    companion object {
        const val MAX_NOMBRE = 40
    }
}
