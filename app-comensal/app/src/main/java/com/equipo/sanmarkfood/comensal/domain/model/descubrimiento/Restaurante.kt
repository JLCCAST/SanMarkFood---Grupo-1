package com.equipo.sanmarkfood.comensal.domain.model.descubrimiento

data class Coordenadas(
    val latitud: Double,
    val longitud: Double
)

enum class CategoriaRestaurante(val valor: String) {
    CRIOLLA("criolla"),
    CHIFA("chifa"),
    POLLERIA("polleria"),
    MARINA("marina"),
    VEGETARIANA("vegetariana"),
    OTRA("otra");

    companion object {
        /** Traduce el valor guardado en Firestore; cualquier valor desconocido cuenta como "otra". */
        fun desde(valor: String?): CategoriaRestaurante =
            entries.firstOrNull { it.valor == valor } ?: OTRA
    }
}

data class Restaurante(
    val id: String,
    val nombre: String,
    val direccion: String,
    val categoria: CategoriaRestaurante,
    val ubicacion: Coordenadas?,
    val portadaUrl: String,
    val logoUrl: String,
    val pausado: Boolean,
    val calificacionPromedio: Double?,
    val totalResenas: Int,
    /** Lo calcula el caso de uso; es null si no hay ubicación del usuario o del local. */
    val distanciaMetros: Int? = null
)