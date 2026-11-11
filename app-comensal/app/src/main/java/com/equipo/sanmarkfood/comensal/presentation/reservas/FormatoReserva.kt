package com.equipo.sanmarkfood.comensal.presentation.reservas

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.comensal.domain.model.reservas.claveDiaDe

private val CLAVES_DE_DIA = listOf("lun", "mar", "mie", "jue", "vie", "sab", "dom")

private fun partesDe(fecha: String): Pair<Int, Int> {
    val partes = fecha.split("-")
    val mes = partes.getOrNull(1)?.toIntOrNull() ?: 1
    val dia = partes.getOrNull(2)?.toIntOrNull() ?: 1
    return dia to mes
}

@Composable
fun diaCorto(clave: String): String =
    stringArrayResource(R.array.reserva_dias_cortos).getOrElse(CLAVES_DE_DIA.indexOf(clave)) { "" }

@Composable
fun fechaCorta(fecha: String): String {
    val (dia, mes) = partesDe(fecha)
    val meses = stringArrayResource(R.array.reserva_meses_cortos)
    return "${diaCorto(claveDiaDe(fecha).orEmpty())} $dia ${meses.getOrElse(mes - 1) { "" }}"
}

@Composable
fun fechaLarga(fecha: String): String {
    val (dia, mes) = partesDe(fecha)
    val dias = stringArrayResource(R.array.reserva_dias_largos)
    val meses = stringArrayResource(R.array.reserva_meses)
    val nombreDia = dias.getOrElse(CLAVES_DE_DIA.indexOf(claveDiaDe(fecha).orEmpty())) { "" }
    return "$nombreDia $dia de ${meses.getOrElse(mes - 1) { "" }}"
}

@Composable
fun etiquetaComensales(personas: Int): String =
    pluralStringResource(R.plurals.reserva_comensales, personas, personas)

@StringRes
fun mensajeDeErrorReserva(error: ErrorReservas): Int = when (error) {
    ErrorReservas.FueraDePlazo -> R.string.reserva_error_plazo
    ErrorReservas.FueraDeHorario -> R.string.reserva_error_horario
    ErrorReservas.FranjaLlena -> R.string.reserva_error_franja_llena
    ErrorReservas.TopeReservas -> R.string.reserva_error_tope
    ErrorReservas.SinReservas -> R.string.reserva_error_sin_reservas
    ErrorReservas.ComensalesInvalidos -> R.string.reserva_error_comensales
    ErrorReservas.SesionInvalida -> R.string.reserva_error_sesion
    ErrorReservas.EstadoInvalido -> R.string.reserva_error_estado
    ErrorReservas.SinCambios -> R.string.reserva_error_sin_cambios
    ErrorReservas.SinConexion, ErrorReservas.Desconocido -> R.string.reserva_error_desconocido
}
