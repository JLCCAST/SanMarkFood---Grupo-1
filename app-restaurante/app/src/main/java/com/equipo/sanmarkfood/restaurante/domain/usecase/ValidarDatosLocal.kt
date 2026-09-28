package com.equipo.sanmarkfood.restaurante.domain.usecase

import com.equipo.sanmarkfood.restaurante.domain.model.CampoLocal
import com.equipo.sanmarkfood.restaurante.domain.model.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Ubicacion

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

// Celular de 9 dígitos que empieza con 9, fijo de Lima de 7, o fijo con código de ciudad (01…, 044…).
// Acepta espacios, guiones, paréntesis y el prefijo +51; devuelve solo los dígitos, o null si no es válido.
private fun normalizarTelefono(texto: String): String? {
    if (texto.any { it !in '0'..'9' && it !in " +-()" }) return null
    val digitos = texto.filter { it in '0'..'9' }
        .let { if (it.length == 11 && it.startsWith("51")) it.drop(2) else it }
    val valido = digitos.length == 7 || (digitos.length == 9 && digitos.first() in "09")
    return digitos.takeIf { valido }
}
