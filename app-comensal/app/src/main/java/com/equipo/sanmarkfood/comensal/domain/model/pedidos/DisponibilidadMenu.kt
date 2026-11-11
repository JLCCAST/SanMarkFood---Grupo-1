package com.equipo.sanmarkfood.comensal.domain.model.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.EstadoMenuDelDia

sealed interface DisponibilidadMenu {
    data class Disponible(val menu: MenuDeHoy) : DisponibilidadMenu

    data object Agotado : DisponibilidadMenu

    data class Terminado(val horaFin: String) : DisponibilidadMenu

    data object SinPublicar : DisponibilidadMenu
}

fun disponibilidadDelMenu(menu: MenuDeHoy?, horaActual: String): DisponibilidadMenu = when {
    menu == null -> DisponibilidadMenu.SinPublicar
    horaActual >= menu.horaFin -> DisponibilidadMenu.Terminado(menu.horaFin)
    menu.estado == EstadoMenuDelDia.TERMINADO -> DisponibilidadMenu.Agotado
    menu.entradas.all { it.agotado } || menu.segundos.all { it.agotado } -> DisponibilidadMenu.Agotado
    else -> DisponibilidadMenu.Disponible(menu)
}
