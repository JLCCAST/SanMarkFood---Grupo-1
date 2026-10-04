package com.equipo.sanmarkfood.restaurante.domain.model.menu

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun fechaDeHoy(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
        .apply { timeZone = TimeZone.getTimeZone("America/Lima") }
        .format(Date())
