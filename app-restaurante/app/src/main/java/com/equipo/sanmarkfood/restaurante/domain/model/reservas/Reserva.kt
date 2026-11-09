package com.equipo.sanmarkfood.restaurante.domain.model.reservas

const val DIAS_VISIBLES = 7

const val MINUTOS_ANTES_DEL_RECHAZO = 60

enum class EstadoReserva { PENDIENTE, CONFIRMADA, RECHAZADA, CANCELADA }

enum class MotivoRechazoReserva { SIN_MESAS, LOCAL_CERRADO, GRUPO_GRANDE, OTRO }

data class HorarioReserva(val fecha: String, val hora: String)

data class Reserva(
    val id: String,
    val comensalNombre: String,
    val fecha: String,
    val hora: String,
    val personas: Int,
    val estado: EstadoReserva,
    val anterior: HorarioReserva?,
)

data class ReservasDeLaSemana(val reservas: List<Reserva>, val sinConexion: Boolean)
