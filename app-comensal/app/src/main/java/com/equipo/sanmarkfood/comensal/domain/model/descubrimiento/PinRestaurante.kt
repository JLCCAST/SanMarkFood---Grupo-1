package com.equipo.sanmarkfood.comensal.domain.model.descubrimiento

/** Lo que muestra el pin de un local en el mapa  */
sealed interface EtiquetaPin {
    /** Precio del menú de hoy, en céntimos. */
    data class Precio(val centimos: Int) : EtiquetaPin

    /** No hay menú de hoy: el pin dice el nombre del restaurante. */
    data class Nombre(val texto: String) : EtiquetaPin

    /** El local está pausado: el pin dice «Cerrado». */
    data object Cerrado : EtiquetaPin
}

/**
 * Menú del día que se puede mostrar: es de hoy (hora de Lima) y está publicado.
 * El campo menuHoy no se borra solo al cambiar el día, por eso se compara la fecha.
 */
fun Restaurante.menuVigente(): MenuDelDia? =
    menuHoy?.takeIf { it.fecha == fechaDeHoy() && it.estado == EstadoMenuDelDia.PUBLICADO }

fun Restaurante.etiquetaPin(): EtiquetaPin = when {
    pausado -> EtiquetaPin.Cerrado
    else -> menuVigente()?.let { EtiquetaPin.Precio(it.precio) } ?: EtiquetaPin.Nombre(nombre)
}