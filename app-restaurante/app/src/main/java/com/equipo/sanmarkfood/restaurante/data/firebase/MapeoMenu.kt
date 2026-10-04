package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosPlato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.EstadoMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.MenuDelDia
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OpcionMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OrigenMenu
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

internal fun DatosMenu.aCampos(): Map<String, Any> = buildMap {
    put("precio", precio)
    put("entradas", entradas.map { it.aCampos() })
    put("segundos", segundos.map { it.aCampos() })
    refresco?.let { put("refresco", it) }
    postre?.let { put("postre", it) }
    put("horaFin", horaFin)
}

private fun OpcionMenu.aCampos(): Map<String, Any> = mapOf("nombre" to nombre, "agotado" to agotado)

internal fun DocumentSnapshot.aMenuDelDia(): MenuDelDia = MenuDelDia(
    fecha = id,
    datos = DatosMenu(
        precio = getLong("precio")?.toInt() ?: throw ErrorMenu.Desconocido,
        entradas = opcionesDe(get("entradas")),
        segundos = opcionesDe(get("segundos")),
        refresco = getString("refresco"),
        postre = getString("postre"),
        horaFin = getString("horaFin") ?: throw ErrorMenu.Desconocido,
    ),
    estado = EstadoMenu.entries.firstOrNull { it.valor() == getString("estado") } ?: throw ErrorMenu.Desconocido,
    origen = OrigenMenu.entries.firstOrNull { it.valor() == getString("origen") } ?: throw ErrorMenu.Desconocido,
    publicadoEn = getTimestamp("publicadoEn", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDate(),
)

private fun opcionesDe(valor: Any?): List<OpcionMenu> =
    (valor as? List<*>).orEmpty().map { opcion ->
        val campos = opcion as? Map<*, *> ?: throw ErrorMenu.Desconocido
        OpcionMenu(
            nombre = campos["nombre"] as? String ?: throw ErrorMenu.Desconocido,
            agotado = campos["agotado"] as? Boolean ?: false,
        )
    }

internal fun EstadoMenu.valor(): String = when (this) {
    EstadoMenu.PUBLICADO -> "publicado"
    EstadoMenu.TERMINADO -> "terminado"
}

internal fun OrigenMenu.valor(): String = when (this) {
    OrigenMenu.AYER -> "ayer"
    OrigenMenu.CERO -> "cero"
    OrigenMenu.IA -> "ia"
}
