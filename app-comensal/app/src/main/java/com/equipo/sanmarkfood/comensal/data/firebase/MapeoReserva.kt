package com.equipo.sanmarkfood.comensal.data.firebase

import com.equipo.sanmarkfood.comensal.domain.model.reservas.EstadoReserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.HorarioAnterior
import com.equipo.sanmarkfood.comensal.domain.model.reservas.MotivoRechazoReserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.RechazoReserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.google.firebase.firestore.DocumentSnapshot

internal fun DocumentSnapshot.aReserva(): Reserva? {
    return Reserva(
        id = id,
        restauranteId = getString("restauranteUid") ?: return null,
        restauranteNombre = getString("restauranteNombre").orEmpty(),
        fecha = getString("fecha") ?: return null,
        hora = getString("hora") ?: return null,
        personas = getLong("personas")?.toInt() ?: return null,
        estado = opcionDe<EstadoReserva>(getString("estado")) ?: return null,
        rechazo = (get("rechazo") as? Map<*, *>)?.aRechazoReserva(),
        horarioAnterior = (get("horarioAnterior") as? Map<*, *>)?.aHorarioAnterior(),
        creadoEn = getTimestamp("creadoEn")?.toDate()?.time,
        respondidaEn = getTimestamp("respondidaEn")?.toDate()?.time,
        actualizadoEn = getTimestamp("actualizadoEn")?.toDate()?.time,
    )
}

internal fun DocumentSnapshot.aOcupacion(): Pair<String, Int>? {
    val hora = id.substringAfter('_', "").takeIf { it.length == 4 } ?: return null
    val ocupados = getLong("ocupados")?.toInt() ?: return null
    return "${hora.take(2)}:${hora.drop(2)}" to ocupados
}

private fun Map<*, *>.aRechazoReserva(): RechazoReserva? {
    return RechazoReserva(
        motivo = opcionDe<MotivoRechazoReserva>(this["motivo"] as? String) ?: return null,
        detalle = (this["detalle"] as? String)?.takeIf { it.isNotBlank() },
        automatico = this["automatico"] as? Boolean ?: false,
    )
}

private fun Map<*, *>.aHorarioAnterior(): HorarioAnterior? {
    return HorarioAnterior(
        fecha = this["fecha"] as? String ?: return null,
        hora = this["hora"] as? String ?: return null,
    )
}

private inline fun <reified T : Enum<T>> opcionDe(valor: String?): T? =
    enumValues<T>().firstOrNull { it.name.lowercase() == valor }

