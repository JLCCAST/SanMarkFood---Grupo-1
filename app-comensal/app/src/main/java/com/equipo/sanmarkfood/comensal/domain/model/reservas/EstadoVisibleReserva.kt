package com.equipo.sanmarkfood.comensal.domain.model.reservas

enum class EstadoVisibleReserva { PENDIENTE, CONFIRMADA, COMPLETADA, SIN_RESPUESTA, RECHAZADA, CANCELADA }

fun Reserva.estadoVisible(ahora: Long): EstadoVisibleReserva = when (estado) {
    EstadoReserva.PENDIENTE ->
        if (yaPaso(ahora)) EstadoVisibleReserva.SIN_RESPUESTA else EstadoVisibleReserva.PENDIENTE
    EstadoReserva.CONFIRMADA ->
        if (yaPaso(ahora)) EstadoVisibleReserva.COMPLETADA else EstadoVisibleReserva.CONFIRMADA
    EstadoReserva.RECHAZADA ->
        if (rechazo?.automatico == true || rechazo?.motivo == MotivoRechazoReserva.SIN_RESPUESTA) {
            EstadoVisibleReserva.SIN_RESPUESTA
        } else {
            EstadoVisibleReserva.RECHAZADA
        }
    EstadoReserva.CANCELADA -> EstadoVisibleReserva.CANCELADA
}

fun Reserva.sigueEnCurso(ahora: Long): Boolean = vigente && !yaPaso(ahora)

fun List<Reserva>.proximaReserva(ahora: Long): Reserva? =
    filter { it.sigueEnCurso(ahora) }.minByOrNull { it.momento ?: Long.MAX_VALUE }
