package com.equipo.sanmarkfood.restaurante.domain.model

/** Lo que el local llena en R3. El teléfono se guarda solo con dígitos. */
data class DatosLocal(
    val nombre: String,
    val categoria: CategoriaRestaurante,
    val direccion: String,
    val ubicacion: Ubicacion,
    val telefono: String,
) {
    companion object {
        const val MAX_NOMBRE = 80
        const val MAX_DIRECCION = 200
    }
}

enum class CampoLocal {
    NOMBRE,
    CATEGORIA,
    DIRECCION,
    TELEFONO,
}
