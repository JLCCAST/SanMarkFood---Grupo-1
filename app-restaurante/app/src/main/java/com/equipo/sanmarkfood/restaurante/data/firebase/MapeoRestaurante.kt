package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Restaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Ubicacion
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint

// Campos del documento restaurantes/{uid}. Los mismos nombres y valores los validan las reglas de Firestore.

internal fun DatosLocal.aCampos(): Map<String, Any> = mapOf(
    "nombre" to nombre,
    "categoria" to categoria.valor(),
    "direccion" to direccion,
    "ubicacion" to GeoPoint(ubicacion.latitud, ubicacion.longitud),
    "telefono" to telefono,
)

internal fun DocumentSnapshot.aRestaurante(): Restaurante? {
    if (!exists()) return null
    val punto = getGeoPoint("ubicacion") ?: throw ErrorRestaurante.Desconocido
    return Restaurante(
        datos = DatosLocal(
            nombre = getString("nombre").orEmpty(),
            categoria = CategoriaRestaurante.entries.firstOrNull { it.valor() == getString("categoria") }
                ?: throw ErrorRestaurante.Desconocido,
            direccion = getString("direccion").orEmpty(),
            ubicacion = Ubicacion(punto.latitude, punto.longitude),
            telefono = getString("telefono").orEmpty(),
        ),
        estado = EstadoRestaurante.entries.firstOrNull { it.valor() == getString("estado") }
            ?: throw ErrorRestaurante.Desconocido,
    )
}

internal fun EstadoRestaurante.valor(): String = when (this) {
    EstadoRestaurante.BORRADOR -> "borrador"
    EstadoRestaurante.PENDIENTE -> "pendiente"
    EstadoRestaurante.APROBADO -> "aprobado"
    EstadoRestaurante.RECHAZADO -> "rechazado"
}

private fun CategoriaRestaurante.valor(): String = when (this) {
    CategoriaRestaurante.CRIOLLA -> "criolla"
    CategoriaRestaurante.CHIFA -> "chifa"
    CategoriaRestaurante.POLLERIA -> "polleria"
    CategoriaRestaurante.MARINA -> "marina"
    CategoriaRestaurante.VEGETARIANA -> "vegetariana"
    CategoriaRestaurante.OTRA -> "otra"
}
