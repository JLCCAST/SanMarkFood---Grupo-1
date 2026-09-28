package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.DiaSemana
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Hora
import com.equipo.sanmarkfood.restaurante.domain.model.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.HorarioDia
import com.equipo.sanmarkfood.restaurante.domain.model.Restaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Ubicacion
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import java.util.Locale

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
        horario = (get("horario") as? Map<*, *>)?.aHorario(),
    )
}

// horario: { lun: { abierto: true, abre: "11:30", cierra: "16:00" }, mar: {…}, … dom: {…} }
internal fun Horario.aCampos(): Map<String, Any> = dias.entries.associate { (dia, horarioDia) ->
    dia.clave() to mapOf(
        "abierto" to horarioDia.abierto,
        "abre" to horarioDia.abre.aTexto(),
        "cierra" to horarioDia.cierra.aTexto(),
    )
}

private fun Map<*, *>.aHorario(): Horario = Horario(
    DiaSemana.entries.associateWith { dia ->
        val campos = this[dia.clave()] as? Map<*, *> ?: throw ErrorRestaurante.Desconocido
        HorarioDia(
            abierto = campos["abierto"] as? Boolean ?: throw ErrorRestaurante.Desconocido,
            abre = horaDe(campos["abre"]),
            cierra = horaDe(campos["cierra"]),
        )
    }
)

private fun DiaSemana.clave(): String = when (this) {
    DiaSemana.LUNES -> "lun"
    DiaSemana.MARTES -> "mar"
    DiaSemana.MIERCOLES -> "mie"
    DiaSemana.JUEVES -> "jue"
    DiaSemana.VIERNES -> "vie"
    DiaSemana.SABADO -> "sab"
    DiaSemana.DOMINGO -> "dom"
}

// "HH:mm" con ceros a la izquierda: así las reglas pueden comparar abre < cierra como texto.
private fun Hora.aTexto(): String = "%02d:%02d".format(Locale.ROOT, hora, minuto)

private fun horaDe(valor: Any?): Hora {
    val partes = (valor as? String)?.split(":")?.mapNotNull { it.toIntOrNull() }
    if (partes == null || partes.size != 2) throw ErrorRestaurante.Desconocido
    return Hora(partes[0], partes[1])
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
