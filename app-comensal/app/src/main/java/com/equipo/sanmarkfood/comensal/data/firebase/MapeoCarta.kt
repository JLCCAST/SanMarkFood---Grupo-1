package com.equipo.sanmarkfood.comensal.data.firebase

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.EstadoMenuDelDia
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.CategoriaCarta
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MenuDeHoy
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.OpcionMenu
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Plato
import com.google.firebase.firestore.DocumentSnapshot

internal fun DocumentSnapshot.aCategoriaCarta(): CategoriaCarta? {
    val nombre = getString("nombre") ?: return null
    val orden = getLong("orden")?.toInt() ?: return null
    return CategoriaCarta(id = id, nombre = nombre, orden = orden)
}

internal fun DocumentSnapshot.aMenuDeHoy(): MenuDeHoy? {
    val precio = getLong("precio")?.toInt() ?: return null
    val estado = EstadoMenuDelDia.desde(getString("estado")) ?: return null
    return MenuDeHoy(
        fecha = id,
        precio = precio,
        entradas = aOpcionesMenu(get("entradas")),
        segundos = aOpcionesMenu(get("segundos")),
        refresco = getString("refresco")?.takeIf { it.isNotBlank() },
        postre = getString("postre")?.takeIf { it.isNotBlank() },
        horaFin = getString("horaFin") ?: HORA_FIN_POR_DEFECTO,
        estado = estado,
    )
}

private const val HORA_FIN_POR_DEFECTO = "15:00"

private fun aOpcionesMenu(valor: Any?): List<OpcionMenu> =
    (valor as? List<*>).orEmpty().mapNotNull { opcion ->
        val datos = opcion as? Map<*, *> ?: return@mapNotNull null
        val nombre = datos["nombre"] as? String ?: return@mapNotNull null
        OpcionMenu(nombre = nombre, agotado = datos["agotado"] as? Boolean ?: false)
    }

internal fun DocumentSnapshot.aPlato(): Plato? {
    val nombre = getString("nombre") ?: return null
    val precio = getLong("precio")?.toInt() ?: return null
    val categoriaId = getString("categoriaId") ?: return null
    return Plato(
        id = id,
        nombre = nombre,
        descripcion = getString("descripcion"),
        precio = precio,
        categoriaId = categoriaId,
        fotoUrl = getString("fotoUrl"),
        agotadoEl = getString("agotadoEl"),
    )
}