package com.equipo.sanmarkfood.restaurante.domain.model.reservas

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Hora
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.HorarioDia
import java.util.Locale

data class Franja(val hora: String, val confirmadas: List<Reserva>, val pendientes: Int) {
    val confirmados: Int get() = confirmadas.sumOf { it.personas }

    fun llena(cupo: Int): Boolean = confirmados + pendientes >= cupo
}

fun franjasDelDia(horarioDia: HorarioDia?, reservas: List<Reserva>): List<Franja> {
    val delHorario = if (horarioDia?.abierto == true) horasEntre(horarioDia.abre, horarioDia.cierra) else emptyList()
    return (delHorario + reservas.map { it.hora }).distinct().sorted().map { hora ->
        val enLaFranja = reservas.filter { it.hora == hora }
        Franja(
            hora = hora,
            confirmadas = enLaFranja.filter { it.estado == EstadoReserva.CONFIRMADA },
            pendientes = enLaFranja.filter { it.estado == EstadoReserva.PENDIENTE }.sumOf { it.personas },
        )
    }
}

fun horaDelRechazo(hora: String): String {
    val (horas, minutos) = hora.split(":").map { it.toInt() }
    return textoHora((horas * 60 + minutos - MINUTOS_ANTES_DEL_RECHAZO + 1440) % 1440)
}

private fun horasEntre(abre: Hora, cierra: Hora): List<String> {
    val inicio = (abre.hora * 60 + abre.minuto + 29) / 30 * 30
    val fin = cierra.hora * 60 + cierra.minuto
    return (inicio until fin step 30).map { textoHora(it) }
}

private fun textoHora(minutos: Int): String = "%02d:%02d".format(Locale.ROOT, minutos / 60, minutos % 60)
