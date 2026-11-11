package com.equipo.sanmarkfood.comensal.domain.model.reservas

enum class EstadoReserva { PENDIENTE, CONFIRMADA, RECHAZADA, CANCELADA }

enum class MotivoRechazoReserva { SIN_MESAS, LOCAL_CERRADO, GRUPO_GRANDE, OTRO, SIN_RESPUESTA }

data class RechazoReserva(
    val motivo: MotivoRechazoReserva,
    val detalle: String?,
    val automatico: Boolean,
)

data class HorarioAnterior(
    val fecha: String,
    val hora: String,
)

data class Reserva(
    val id: String,
    val restauranteId: String,
    val restauranteNombre: String,
    val fecha: String,
    val hora: String,
    val personas: Int,
    val estado: EstadoReserva,
    val rechazo: RechazoReserva?,
    val horarioAnterior: HorarioAnterior?,
    val creadoEn: Long?,
    val respondidaEn: Long?,
    val actualizadoEn: Long?,
) {
    val vigente: Boolean
        get() = estado == EstadoReserva.PENDIENTE || estado == EstadoReserva.CONFIRMADA

    val momento: Long?
        get() = momentoDeReserva(fecha, hora)

    val limiteDeRespuesta: Long?
        get() = momento?.minus(MINUTOS_DE_RESPUESTA * MILIS_POR_MINUTO)

    fun estaActiva(hoy: String): Boolean = vigente && fecha >= hoy

    fun yaPaso(ahora: Long): Boolean = (momento ?: 0L) <= ahora
}

fun List<Reserva>.activas(hoy: String): List<Reserva> = filter { it.estaActiva(hoy) }

fun List<Reserva>.alcanzoElTope(hoy: String): Boolean = activas(hoy).size >= MAX_RESERVAS_ACTIVAS
