package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.EstadoReserva
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.HorarioReserva
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.Reserva
import com.google.firebase.firestore.DocumentSnapshot

internal fun DocumentSnapshot.aReserva(): Reserva = Reserva(
    id = id,
    comensalNombre = getString("comensalNombre").orEmpty(),
    fecha = getString("fecha") ?: throw ErrorReservas.Desconocido,
    hora = getString("hora") ?: throw ErrorReservas.Desconocido,
    personas = getLong("personas")?.toInt() ?: throw ErrorReservas.Desconocido,
    estado = EstadoReserva.entries.firstOrNull { it.name.lowercase() == getString("estado") }
        ?: throw ErrorReservas.Desconocido,
    anterior = (get("horarioAnterior") as? Map<*, *>)?.aHorarioReserva(),
)

private fun Map<*, *>.aHorarioReserva(): HorarioReserva? {
    val fecha = this["fecha"] as? String ?: return null
    val hora = this["hora"] as? String ?: return null
    return HorarioReserva(fecha, hora)
}
