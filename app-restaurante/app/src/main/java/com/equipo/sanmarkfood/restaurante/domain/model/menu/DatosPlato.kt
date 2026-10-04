package com.equipo.sanmarkfood.restaurante.domain.model.menu

data class DatosPlato(
    val nombre: String,
    val descripcion: String?,
    val precio: Int,
    val categoriaId: String,
) {
    companion object {
        const val MAX_NOMBRE = 80
        const val MAX_DESCRIPCION = 300
        const val PRECIO_MAXIMO = 100_000
    }
}
