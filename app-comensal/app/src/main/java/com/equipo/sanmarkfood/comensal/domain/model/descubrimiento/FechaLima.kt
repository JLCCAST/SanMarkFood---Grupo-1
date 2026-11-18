package com.equipo.sanmarkfood.comensal.domain.model.descubrimiento

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val zonaLima = TimeZone.getTimeZone("America/Lima")

private fun formatoFecha() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { timeZone = zonaLima }

fun fechaDeHoy(): String = formatoFecha().format(Date())

private fun formatoHora() = SimpleDateFormat("HH:mm", Locale.ROOT).apply { timeZone = zonaLima }

fun horaActualLima(): String = formatoHora().format(Date())