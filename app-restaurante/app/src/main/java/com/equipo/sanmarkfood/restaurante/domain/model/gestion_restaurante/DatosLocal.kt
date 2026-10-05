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
    val telefonoNacional: String get() = telefono.removePrefix(PREFIJO_TELEFONO)

    companion object {
        const val MAX_NOMBRE = 80
        const val MAX_DIRECCION = 200
        const val DIGITOS_TELEFONO = 9
        const val PREFIJO_TELEFONO = "+51"
    }
}

enum class CampoLocal {
    PORTADA,
    NOMBRE,
    CATEGORIA,
    DIRECCION,
    TELEFONO,
}
