package com.equipo.sanmarkfood.comensal.data.firebase

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.CategoriaCarta
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Plato
import com.google.firebase.firestore.DocumentSnapshot

internal fun DocumentSnapshot.aCategoriaCarta(): CategoriaCarta? {
    val nombre = getString("nombre") ?: return null
    val orden = getLong("orden")?.toInt() ?: return null
    return CategoriaCarta(id = id, nombre = nombre, orden = orden)
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