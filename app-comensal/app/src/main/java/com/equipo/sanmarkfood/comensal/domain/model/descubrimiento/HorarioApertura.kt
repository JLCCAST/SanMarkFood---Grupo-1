package com.equipo.sanmarkfood.comensal.domain.model.descubrimiento

import java.util.Calendar
import java.util.TimeZone

private val ZONA_APERTURA: TimeZone = TimeZone.getTimeZone("America/Lima")

/** Clave del día en el `horario` de Firestore: `lun`, `mar`, `mie`, `jue`, `vie`, `sab`, `dom`. */
private fun Int.claveDia(): String? = when (this) {
    Calendar.MONDAY -> "lun"
    Calendar.TUESDAY -> "mar"
    Calendar.WEDNESDAY -> "mie"
    Calendar.THURSDAY -> "jue"
    Calendar.FRIDAY -> "vie"
    Calendar.SATURDAY -> "sab"
    Calendar.SUNDAY -> "dom"
    else -> null
}

/** Convierte «HH:mm» en minutos desde la medianoche; null si el texto no tiene ese formato. */
private fun minutosDelDia(texto: String): Int? {
    val partes = texto.split(":")
    if (partes.size != 2) return null
    val horas = partes[0].toIntOrNull() ?: return null
    val minutos = partes[1].toIntOrNull() ?: return null
    if (horas !in 0..23 || minutos !in 0..59) return null
    return horas * 60 + minutos
}

/**
 * true si el local está abierto en ese momento: no está pausado, hoy abre según su `horario`
 * y la hora cae entre `abre` y `cierra`. Un local sin horario de hoy cuenta como cerrado.
 */
fun Restaurante.estaAbiertoAhora(
    ahora: Calendar = Calendar.getInstance(ZONA_APERTURA)
): Boolean {
    if (pausado) return false

    val clave = ahora.get(Calendar.DAY_OF_WEEK).claveDia() ?: return false
    val dia = horario[clave] ?: return false
    if (!dia.abierto) return false

    val abre = minutosDelDia(dia.abre) ?: return false
    val cierra = minutosDelDia(dia.cierra) ?: return false
    val minutoActual = ahora.get(Calendar.HOUR_OF_DAY) * 60 + ahora.get(Calendar.MINUTE)

    return if (cierra > abre) {
        minutoActual in abre until cierra
    } else {
        // Cierra después de medianoche (por ejemplo, de 18:00 a 02:00).
        minutoActual >= abre || minutoActual < cierra
    }
}