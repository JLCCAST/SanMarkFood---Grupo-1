package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.reservas.MotivoRechazoReserva
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.ReservasDeLaSemana
import kotlinx.coroutines.flow.Flow

interface ReservasRepository {
    fun observarReservasEntre(desde: String, hasta: String): Flow<ReservasDeLaSemana>

    suspend fun confirmar(reservaId: String)

    suspend fun rechazar(reservaId: String, motivo: MotivoRechazoReserva, detalle: String?)
}
