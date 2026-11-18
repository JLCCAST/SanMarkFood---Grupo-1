package com.equipo.sanmarkfood.comensal.data.firebase

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.CategoriaRestaurante
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Coordenadas
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.EstadoMenuDelDia
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.HorarioDia
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.MenuDelDia
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.RangoCarta
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import com.google.firebase.firestore.DocumentSnapshot

internal fun DocumentSnapshot.aRestaurante(): Restaurante? {
    val nombre = getString("nombre") ?: return null
    val punto = getGeoPoint("ubicacion")
    val calificacion = get("calificacion") as? Map<*, *>

    return Restaurante(
        id = id,
        nombre = nombre,
        direccion = getString("direccion").orEmpty(),
        categoria = CategoriaRestaurante.desde(getString("categoria")),
        ubicacion = punto?.let { Coordenadas(it.latitude, it.longitude) },
        portadaUrl = getString("portadaUrl").orEmpty(),
        logoUrl = getString("logoUrl").orEmpty(),
        pausado = getBoolean("pausado") ?: false,
        calificacionPromedio = (calificacion?.get("promedio") as? Number)?.toDouble(),
        totalResenas = (calificacion?.get("total") as? Number)?.toInt() ?: 0,
        resumenResenas = ((get("resumenResenas") as? Map<*, *>)?.get("texto") as? String)?.takeIf { it.isNotBlank() },
        menuHoy = (get("menuHoy") as? Map<*, *>)?.aMenuDelDia(),
        rangoCarta = (get("rangoCarta") as? Map<*, *>)?.aRangoCarta(),
        horario = (get("horario") as? Map<*, *>)?.aHorario().orEmpty(),
        telefono = getString("telefono").orEmpty(),
        aceptaReservas = getBoolean("aceptaReservas") ?: true,
        cupoPorFranja = getLong("cupoPorFranja")?.toInt()
    )
}

/** null si falta `fecha` o `precio`: sin esos dos no se puede usar el menú para filtrar. */
private fun Map<*, *>.aMenuDelDia(): MenuDelDia? {
    val fecha = this["fecha"] as? String ?: return null
    val precio = (this["precio"] as? Number)?.toInt() ?: return null
    return MenuDelDia(
        fecha = fecha,
        precio = precio,
        horaFin = this["horaFin"] as? String ?: "",
        estado = EstadoMenuDelDia.desde(this["estado"] as? String)
    )
}

/** null si falta `min` o `max`. */
private fun Map<*, *>.aRangoCarta(): RangoCarta? {
    val min = (this["min"] as? Number)?.toInt() ?: return null
    val max = (this["max"] as? Number)?.toInt() ?: return null
    return RangoCarta(min = min, max = max)
}

/** Lee `lun` a `dom`; un día sin `abre` o `cierra` se descarta y cuenta como sin horario. */
private fun Map<*, *>.aHorario(): Map<String, HorarioDia> {
    val dias = mutableMapOf<String, HorarioDia>()
    for ((clave, valor) in this) {
        val dia = clave as? String ?: continue
        val datos = valor as? Map<*, *> ?: continue
        val abre = datos["abre"] as? String ?: continue
        val cierra = datos["cierra"] as? String ?: continue
        dias[dia] = HorarioDia(
            abierto = datos["abierto"] as? Boolean ?: false,
            abre = abre,
            cierra = cierra
        )
    }
    return dias
}