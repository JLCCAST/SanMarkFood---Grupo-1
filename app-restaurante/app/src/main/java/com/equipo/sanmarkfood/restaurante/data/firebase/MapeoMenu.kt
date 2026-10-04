package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosPlato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.Plato
import com.google.firebase.firestore.DocumentSnapshot

internal fun DocumentSnapshot.aCategoria(): Categoria = Categoria(
    id = id,
    nombre = getString("nombre") ?: throw ErrorMenu.Desconocido,
    orden = getLong("orden")?.toInt() ?: throw ErrorMenu.Desconocido,
)

internal fun DatosPlato.aCampos(): Map<String, Any> = buildMap {
    put("nombre", nombre)
    descripcion?.let { put("descripcion", it) }
    put("precio", precio)
    put("categoriaId", categoriaId)
}

internal fun DocumentSnapshot.aPlato(): Plato = Plato(
    id = id,
    datos = DatosPlato(
        nombre = getString("nombre") ?: throw ErrorMenu.Desconocido,
        descripcion = getString("descripcion"),
        precio = getLong("precio")?.toInt() ?: throw ErrorMenu.Desconocido,
        categoriaId = getString("categoriaId") ?: throw ErrorMenu.Desconocido,
    ),
    fotoUrl = getString("fotoUrl"),
)
