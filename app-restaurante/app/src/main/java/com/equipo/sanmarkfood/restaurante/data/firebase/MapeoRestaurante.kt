package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.admin.CampoCorregido
import com.equipo.sanmarkfood.restaurante.domain.model.admin.DetalleSolicitud
import com.equipo.sanmarkfood.restaurante.domain.model.admin.SolicitudLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.DiaSemana
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Hora
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.HorarioDia
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.MotivoRechazo
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Rechazo
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Restaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Ubicacion
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import java.util.Locale

// Campos del documento restaurantes/{uid}. Los mismos nombres y valores los validan las reglas de Firestore.

internal fun DatosLocal.aCampos(): Map<String, Any> = buildMap {
    put("nombre", nombre)
    put("categoria", categoria.valor())
    put("direccion", direccion)
    put("ubicacion", GeoPoint(ubicacion.latitud, ubicacion.longitud))
    put("telefono", telefono)
    portadaUrl?.let { put("portadaUrl", it) }
    logoUrl?.let { put("logoUrl", it) }
}

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
            portadaUrl = getString("portadaUrl"),
            logoUrl = getString("logoUrl"),
        ),
        estado = EstadoRestaurante.entries.firstOrNull { it.valor() == getString("estado") }
            ?: throw ErrorRestaurante.Desconocido,
        horario = (get("horario") as? Map<*, *>)?.aHorario(),
        rechazo = (get("rechazo") as? Map<*, *>)?.aRechazo(),
        // Sin el campo, el local recibe pedidos: solo se escribe cuando el local pausa por primera vez.
        pausado = getBoolean("pausado") ?: false,
    )
}

internal fun DocumentSnapshot.aSolicitudLocal(estado: EstadoRestaurante, cantidadPlatos: Int): SolicitudLocal = SolicitudLocal(
    uid = id,
    nombre = getString("nombre").orEmpty(),
    categoria = CategoriaRestaurante.entries.firstOrNull { it.valor() == getString("categoria") }
        ?: CategoriaRestaurante.OTRA,
    logoUrl = getString("logoUrl"),
    estado = estado,
    enviadoEn = getTimestamp("enviadoEn")?.toDate()?.time ?: 0L,
    revisadoEn = getTimestamp("revisadoEn")?.toDate()?.time,
    reenviado = getBoolean("reenviado") == true,
    actualizacion = esActualizacionDeDatos(),
    rechazoAnterior = (get("rechazoAnterior") as? Map<*, *>)?.aRechazo(),
    cantidadPlatos = cantidadPlatos,
)

internal fun DocumentSnapshot.aDetalleSolicitud(
    correo: String?,
    cantidadPlatos: Int,
    cantidadCategorias: Int,
): DetalleSolicitud? {
    val restaurante = aRestaurante() ?: return null
    return DetalleSolicitud(
        uid = id,
        restaurante = restaurante,
        correo = correo,
        reenviado = getBoolean("reenviado") == true,
        actualizacion = esActualizacionDeDatos(),
        rechazoAnterior = (get("rechazoAnterior") as? Map<*, *>)?.aRechazo(),
        camposCorregidos = (get("camposCorregidos") as? List<*>).orEmpty().mapNotNull(::campoCorregidoDe).toSet(),
        revisadoEn = getTimestamp("revisadoEn")?.toDate()?.time,
        cantidadPlatos = cantidadPlatos,
        cantidadCategorias = cantidadCategorias,
    )
}

private fun DocumentSnapshot.esActualizacionDeDatos(): Boolean =
    getBoolean("reenviado") != true && (get("camposCorregidos") as? List<*>).orEmpty().isNotEmpty()

private fun campoCorregidoDe(valor: Any?): CampoCorregido? = when (valor) {
    "nombre" -> CampoCorregido.NOMBRE
    "categoria" -> CampoCorregido.CATEGORIA
    "direccion" -> CampoCorregido.DIRECCION
    "ubicacion" -> CampoCorregido.UBICACION
    "telefono" -> CampoCorregido.TELEFONO
    "portadaUrl" -> CampoCorregido.PORTADA
    "logoUrl" -> CampoCorregido.LOGO
    else -> null
}

internal fun Rechazo.aCampos(): Map<String, Any> = buildMap {
    put("motivos", motivos.sortedBy { it.ordinal }.map { it.valor() })
    detalle?.let { put("detalle", it) }
}

private fun Map<*, *>.aRechazo(): Rechazo {
    val valores = this["motivos"] as? List<*> ?: listOfNotNull(this["motivo"])
    val motivos = valores
        .map { valor -> MotivoRechazo.entries.firstOrNull { it.valor() == valor } ?: MotivoRechazo.OTRO }
        .toSet()
    return Rechazo(
        motivos = motivos.ifEmpty { setOf(MotivoRechazo.OTRO) },
        detalle = (this["detalle"] as? String)?.takeIf { it.isNotBlank() },
    )
}

private fun MotivoRechazo.valor(): String = when (this) {
    MotivoRechazo.DATOS_INCOMPLETOS -> "datos_incompletos"
    MotivoRechazo.DIRECCION_NO_VERIFICABLE -> "direccion_no_verificable"
    MotivoRechazo.LOCAL_DUPLICADO -> "local_duplicado"
    MotivoRechazo.OTRO -> "otro"
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
