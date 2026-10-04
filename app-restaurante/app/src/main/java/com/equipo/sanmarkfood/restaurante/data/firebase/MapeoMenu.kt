package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.google.firebase.firestore.DocumentSnapshot

internal fun DocumentSnapshot.aCategoria(): Categoria = Categoria(
    id = id,
    nombre = getString("nombre") ?: throw ErrorMenu.Desconocido,
    orden = getLong("orden")?.toInt() ?: throw ErrorMenu.Desconocido,
)
