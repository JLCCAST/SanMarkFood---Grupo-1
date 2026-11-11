package com.equipo.sanmarkfood.comensal.domain.repository

import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import kotlinx.coroutines.flow.Flow

interface ReservasRepository {
    fun observarReservas(comensalUid: String): Flow<List<Reserva>>

    fun observarReserva(reservaId: String): Flow<Reserva?>

    fun observarOcupacion(restauranteId: String, fecha: String): Flow<Map<String, Int>>
}
