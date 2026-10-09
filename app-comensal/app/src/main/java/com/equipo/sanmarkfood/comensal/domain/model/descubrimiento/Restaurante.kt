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

enum class EstadoMenuDelDia(val valor: String) {
    PUBLICADO("publicado"),
    TERMINADO("terminado");

    companion object {
        /** null si el valor no viene o no se reconoce: se trata como si no hubiera menú hoy. */
        fun desde(valor: String?): EstadoMenuDelDia? =
            entries.firstOrNull { it.valor == valor }
    }
}

data class MenuDelDia(
    val fecha: String,
    val precio: Int,
    val horaFin: String,
    val estado: EstadoMenuDelDia?
)

/** `rangoCarta` del local, en céntimos: el plato más barato y el más caro de su carta. */
data class RangoCarta(
    val min: Int,
    val max: Int
)

/** Un día del `horario` del local: `abre` y `cierra` vienen como texto «HH:mm». */
data class HorarioDia(
    val abierto: Boolean,
    val abre: String,
    val cierra: String
)

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
    val menuHoy: MenuDelDia? = null,
    val rangoCarta: RangoCarta? = null,
    /** Claves `lun`, `mar`, `mie`, `jue`, `vie`, `sab`, `dom`; vacío si el local no tiene horario. */
    val horario: Map<String, HorarioDia> = emptyMap(),
    /** Lo calcula el caso de uso; es null si no hay ubicación del usuario o del local. */
    val distanciaMetros: Int? = null
)