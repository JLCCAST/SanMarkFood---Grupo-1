package com.equipo.sanmarkfood.restaurante.domain.model.menu

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val zonaLima = TimeZone.getTimeZone("America/Lima")

private fun formatoFecha() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { timeZone = zonaLima }

fun fechaDeHoy(): String = formatoFecha().format(Date())

fun inicioDeHoy(): Date = Calendar.getInstance(zonaLima).apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.time

fun inicioDeSemana(fecha: Date, semanasAtras: Int = 0): Date = Calendar.getInstance(zonaLima).apply {
    firstDayOfWeek = Calendar.MONDAY
    time = fecha
    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    add(Calendar.WEEK_OF_YEAR, -semanasAtras)
}.time

fun fechaDeAyer(): String {
    val ayer = Calendar.getInstance(zonaLima).apply { add(Calendar.DAY_OF_MONTH, -1) }
    return formatoFecha().format(ayer.time)
}
