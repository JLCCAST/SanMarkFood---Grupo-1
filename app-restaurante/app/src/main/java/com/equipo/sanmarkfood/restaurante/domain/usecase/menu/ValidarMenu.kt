package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.CampoMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OpcionMenu

internal fun validarMenu(
    precio: String,
    entradas: List<String>,
    segundos: List<String>,
    refresco: String,
    postre: String,
    horaFin: String,
): DatosMenu {
    val centimos = centimosDe(precio)
    val opcionesDeEntrada = opciones(entradas)
    val opcionesDeSegundo = opciones(segundos)
    val invalidos = buildSet {
        if (centimos == null) add(CampoMenu.PRECIO)
        if (opcionesDeEntrada.isEmpty()) add(CampoMenu.ENTRADAS)
        if (opcionesDeSegundo.isEmpty()) add(CampoMenu.SEGUNDOS)
    }
    if (invalidos.isNotEmpty() || centimos == null) throw ErrorMenu.DatosMenuInvalidos(invalidos)

    return DatosMenu(
        precio = centimos,
        entradas = opcionesDeEntrada,
        segundos = opcionesDeSegundo,
        refresco = refresco.trim().ifEmpty { null },
        postre = postre.trim().ifEmpty { null },
        horaFin = horaFin,
    )
}

private fun opciones(nombres: List<String>): List<OpcionMenu> =
    nombres
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { OpcionMenu(nombre = it, agotado = false) }
