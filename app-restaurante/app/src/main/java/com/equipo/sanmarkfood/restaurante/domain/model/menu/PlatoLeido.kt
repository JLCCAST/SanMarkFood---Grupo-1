package com.equipo.sanmarkfood.restaurante.domain.model.menu

data class PlatoLeido(
    val nombre: String,
    val precio: String,
    val categoria: String,
    val nombreDudoso: Boolean = false,
    val precioDudoso: Boolean = false,
) {
    companion object {
        const val CATEGORIA_POR_DEFECTO = "Platos"
    }
}
