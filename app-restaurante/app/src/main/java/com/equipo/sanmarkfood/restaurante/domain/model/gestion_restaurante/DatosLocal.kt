package com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante

data class DatosLocal(
    val nombre: String,
    val categoria: CategoriaRestaurante,
    val direccion: String,
    val ubicacion: Ubicacion,
    val telefono: String,
    /** Obligatoria al guardar; puede faltar en locales creados antes de SCRUM-63. */
    val portadaUrl: String?,
    /** Opcional: sin logo se muestra la inicial del nombre. */
    val logoUrl: String?,
) {
    companion object {
        const val MAX_NOMBRE = 80
        const val MAX_DIRECCION = 200
    }
}

enum class CampoLocal {
    PORTADA,
    NOMBRE,
    CATEGORIA,
    DIRECCION,
    TELEFONO,
}
