package com.equipo.sanmarkfood.restaurante.domain.model.menu

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val zonaLima = TimeZone.getTimeZone("America/Lima")

private fun formatoFecha() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { timeZone = zonaLima }

fun fechaDeHoy(): String = formatoFecha().format(Date())

fun fechaDeAyer(): String {
    val ayer = Calendar.getInstance(zonaLima).apply { add(Calendar.DAY_OF_MONTH, -1) }
    return formatoFecha().format(ayer.time)
}
