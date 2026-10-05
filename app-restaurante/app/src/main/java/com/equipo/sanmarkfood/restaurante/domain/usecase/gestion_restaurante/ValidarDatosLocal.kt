package com.equipo.sanmarkfood.restaurante.domain.usecase.gestion_restaurante

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CampoLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Ubicacion

/**
 * Reglas de los datos del local que comparten el alta y la edición (guardar) y la corrección (reenviar).
 * Devuelve los datos listos para guardar, o lanza [ErrorRestaurante.DatosInvalidos] con todos los campos mal.
 */
internal fun validarDatosLocal(
    nombre: String,
    categoria: CategoriaRestaurante?,
    direccion: String,
    ubicacion: Ubicacion,
    telefono: String,
    portadaUrl: String?,
    logoUrl: String?,
): DatosLocal {
    val telefonoNormalizado = normalizarTelefono(telefono)
    val invalidos = buildSet {
        if (portadaUrl.isNullOrBlank()) add(CampoLocal.PORTADA)
        if (nombre.isBlank() || nombre.trim().length > DatosLocal.MAX_NOMBRE) add(CampoLocal.NOMBRE)
        if (categoria == null) add(CampoLocal.CATEGORIA)
        if (direccion.isBlank() || direccion.trim().length > DatosLocal.MAX_DIRECCION) add(CampoLocal.DIRECCION)
        if (telefonoNormalizado == null) add(CampoLocal.TELEFONO)
    }
    if (invalidos.isNotEmpty() || categoria == null || telefonoNormalizado == null) {
        throw ErrorRestaurante.DatosInvalidos(invalidos)
    }

    return DatosLocal(
        nombre = nombre.trim(),
        categoria = categoria,
        direccion = direccion.trim(),
        ubicacion = ubicacion,
        telefono = telefonoNormalizado,
        portadaUrl = portadaUrl,
        logoUrl = logoUrl,
    )
}

private val TELEFONO_INTERNACIONAL = Regex("""\+[1-9]\d{7,14}""")
private val CELULAR_PERU = Regex("""\+519\d{8}""")

internal fun normalizarTelefono(texto: String): String? {
    val telefono = texto.filterNot { it in " -()" }
    val valido = TELEFONO_INTERNACIONAL.matches(telefono) &&
        (!telefono.startsWith("+51") || CELULAR_PERU.matches(telefono))
    return telefono.takeIf { valido }
}
