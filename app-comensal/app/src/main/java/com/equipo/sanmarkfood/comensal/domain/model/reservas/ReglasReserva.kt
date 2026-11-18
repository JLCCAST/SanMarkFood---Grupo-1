package com.equipo.sanmarkfood.comensal.domain.model.reservas

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.HorarioDia
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

const val MIN_COMENSALES = 1
const val MAX_COMENSALES = 10
const val MAX_RESERVAS_ACTIVAS = 2
const val DIAS_RESERVABLES = 7
const val MINUTOS_DE_ANTICIPACION = 120
const val MINUTOS_ENTRE_HORARIOS = 30
const val MINUTOS_DE_RESPUESTA = 60

internal const val MILIS_POR_MINUTO = 60_000L
private const val MILIS_POR_DIA = 86_400_000L
private const val MINUTOS_POR_HORA = 60

private val zonaLima: TimeZone = TimeZone.getTimeZone("America/Lima")

data class DiaReservable(
    val fecha: String,
    val claveDia: String,
    val diaDelMes: Int,
    val mes: Int,
    val esHoy: Boolean,
    val cerrado: Boolean,
    val disponible: Boolean,
)

data class HorarioReservable(
    val hora: String,
    val lleno: Boolean,
)

private fun formato(patron: String) =
    SimpleDateFormat(patron, Locale.ROOT).apply {
        timeZone = zonaLima
        isLenient = false
    }

fun momentoDeReserva(fecha: String, hora: String): Long? =
    try {
        formato("yyyy-MM-dd HH:mm").parse("$fecha $hora")?.time
    } catch (_: ParseException) {
        null
    }

fun fechaLimaDe(momento: Long): String = formato("yyyy-MM-dd").format(Date(momento))

fun horaLimaDe(momento: Long): String = formato("HH:mm").format(Date(momento))

private fun calendarioDe(fecha: String): Calendar? {
    val momento = momentoDeReserva(fecha, "12:00") ?: return null
    return Calendar.getInstance(zonaLima).apply { timeInMillis = momento }
}

fun claveDiaDe(fecha: String): String? = when (calendarioDe(fecha)?.get(Calendar.DAY_OF_WEEK)) {
    Calendar.MONDAY -> "lun"
    Calendar.TUESDAY -> "mar"
    Calendar.WEDNESDAY -> "mie"
    Calendar.THURSDAY -> "jue"
    Calendar.FRIDAY -> "vie"
    Calendar.SATURDAY -> "sab"
    Calendar.SUNDAY -> "dom"
    else -> null
}

private fun minutosDelDia(texto: String): Int? {
    val partes = texto.split(":")
    if (partes.size != 2) return null
    val horas = partes[0].toIntOrNull() ?: return null
    val minutos = partes[1].toIntOrNull() ?: return null
    if (horas !in 0..23 || minutos !in 0..59) return null
    return horas * MINUTOS_POR_HORA + minutos
}

private fun textoDeHora(minutos: Int): String =
    "%02d:%02d".format(Locale.ROOT, minutos / MINUTOS_POR_HORA, minutos % MINUTOS_POR_HORA)

fun comensalesValidos(personas: Int): Boolean = personas in MIN_COMENSALES..MAX_COMENSALES

fun dentroDelPlazo(fecha: String, hora: String, ahora: Long): Boolean {
    val momento = momentoDeReserva(fecha, hora) ?: return false
    val desde = ahora + MINUTOS_DE_ANTICIPACION * MILIS_POR_MINUTO
    val hasta = ahora + DIAS_RESERVABLES * MILIS_POR_DIA
    return momento in desde..hasta
}

fun dentroDelHorario(dia: HorarioDia?, hora: String): Boolean {
    if (dia == null || !dia.abierto) return false
    val minuto = minutosDelDia(hora) ?: return false
    val abre = minutosDelDia(dia.abre) ?: return false
    val cierra = minutosDelDia(dia.cierra) ?: return false
    return minuto % MINUTOS_ENTRE_HORARIOS == 0 && minuto >= abre && minuto < cierra
}

fun franjaLlena(ocupados: Int, personas: Int, cupoPorFranja: Int?): Boolean =
    cupoPorFranja != null && ocupados + personas > cupoPorFranja

fun primeraHoraDeHoy(ahora: Long): String {
    val paso = MINUTOS_ENTRE_HORARIOS * MILIS_POR_MINUTO
    val desde = ahora + MINUTOS_DE_ANTICIPACION * MILIS_POR_MINUTO
    return horaLimaDe((desde + paso - 1) / paso * paso)
}

fun horariosDelDia(
    horario: Map<String, HorarioDia>,
    fecha: String,
    ahora: Long,
    personas: Int,
    ocupacion: Map<String, Int> = emptyMap(),
    cupoPorFranja: Int? = null,
): List<HorarioReservable> {
    val dia = claveDiaDe(fecha)?.let { horario[it] } ?: return emptyList()
    if (!dia.abierto) return emptyList()
    val abre = minutosDelDia(dia.abre) ?: return emptyList()
    val cierra = minutosDelDia(dia.cierra) ?: return emptyList()
    val primero = (abre + MINUTOS_ENTRE_HORARIOS - 1) / MINUTOS_ENTRE_HORARIOS * MINUTOS_ENTRE_HORARIOS
    return generateSequence(primero) { it + MINUTOS_ENTRE_HORARIOS }
        .takeWhile { it < cierra }
        .map(::textoDeHora)
        .filter { dentroDelPlazo(fecha, it, ahora) }
        .map { HorarioReservable(it, franjaLlena(ocupacion[it] ?: 0, personas, cupoPorFranja)) }
        .toList()
}

fun diasReservables(horario: Map<String, HorarioDia>, ahora: Long): List<DiaReservable> =
    (0 until DIAS_RESERVABLES).map { desplazamiento ->
        val fecha = fechaLimaDe(ahora + desplazamiento * MILIS_POR_DIA)
        val clave = claveDiaDe(fecha).orEmpty()
        val calendario = calendarioDe(fecha)
        val dia = horario[clave]
        DiaReservable(
            fecha = fecha,
            claveDia = clave,
            diaDelMes = calendario?.get(Calendar.DAY_OF_MONTH) ?: 0,
            mes = (calendario?.get(Calendar.MONTH) ?: 0) + 1,
            esHoy = desplazamiento == 0,
            cerrado = dia == null || !dia.abierto,
            disponible = horariosDelDia(horario, fecha, ahora, MIN_COMENSALES).isNotEmpty(),
        )
    }

fun validarSolicitud(
    solicitud: SolicitudReserva,
    horario: Map<String, HorarioDia>,
    ahora: Long,
    reservas: List<Reserva>,
): ErrorReservas? = when {
    !comensalesValidos(solicitud.personas) -> ErrorReservas.ComensalesInvalidos
    reservas.alcanzoElTope(fechaLimaDe(ahora)) -> ErrorReservas.TopeReservas
    !dentroDelPlazo(solicitud.fecha, solicitud.hora, ahora) -> ErrorReservas.FueraDePlazo
    !dentroDelHorario(claveDiaDe(solicitud.fecha)?.let { horario[it] }, solicitud.hora) -> ErrorReservas.FueraDeHorario
    else -> null
}

fun validarCambio(
    reserva: Reserva,
    fecha: String,
    hora: String,
    horario: Map<String, HorarioDia>,
    ahora: Long,
): ErrorReservas? = when {
    !reserva.vigente -> ErrorReservas.EstadoInvalido
    reserva.fecha == fecha && reserva.hora == hora -> ErrorReservas.SinCambios
    !dentroDelPlazo(fecha, hora, ahora) -> ErrorReservas.FueraDePlazo
    !dentroDelHorario(claveDiaDe(fecha)?.let { horario[it] }, hora) -> ErrorReservas.FueraDeHorario
    else -> null
}
