package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.CampoPlato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosPlato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import java.math.BigDecimal

internal fun validarPlato(
    nombre: String,
    descripcion: String,
    precio: String,
    categoriaId: String?,
): DatosPlato {
    val centimos = centimosDe(precio)
    val invalidos = buildSet {
        if (nombre.isBlank() || nombre.trim().length > DatosPlato.MAX_NOMBRE) add(CampoPlato.NOMBRE)
        if (descripcion.trim().length > DatosPlato.MAX_DESCRIPCION) add(CampoPlato.DESCRIPCION)
        if (centimos == null) add(CampoPlato.PRECIO)
        if (categoriaId.isNullOrBlank()) add(CampoPlato.CATEGORIA)
    }
    if (invalidos.isNotEmpty() || centimos == null || categoriaId == null) {
        throw ErrorMenu.DatosPlatoInvalidos(invalidos)
    }

    return DatosPlato(
        nombre = nombre.trim(),
        descripcion = descripcion.trim().ifEmpty { null },
        precio = centimos,
        categoriaId = categoriaId,
    )
}

internal fun centimosDe(texto: String): Int? {
    val soles = texto.trim().replace(',', '.').toBigDecimalOrNull() ?: return null
    if (soles.scale() > 2) return null

    val centimos = soles.movePointRight(2)
    if (centimos < BigDecimal.ONE || centimos > BigDecimal(DatosPlato.PRECIO_MAXIMO)) return null
    return centimos.toInt()
}
