package com.equipo.sanmarkfood.restaurante.domain.model.menu

enum class EstadoMenu { PUBLICADO, TERMINADO }

enum class OrigenMenu { AYER, CERO, IA }

data class OpcionMenu(val nombre: String, val agotado: Boolean)

data class DatosMenu(
    val precio: Int,
    val entradas: List<OpcionMenu>,
    val segundos: List<OpcionMenu>,
    val refresco: String?,
    val postre: String?,
    val horaFin: String,
) {
    companion object {
        const val MAX_TEXTO = 80
        const val HORA_FIN_POR_DEFECTO = "15:00"
    }
}

data class MenuDelDia(
    val fecha: String,
    val datos: DatosMenu,
    val estado: EstadoMenu,
    val origen: OrigenMenu,
)
