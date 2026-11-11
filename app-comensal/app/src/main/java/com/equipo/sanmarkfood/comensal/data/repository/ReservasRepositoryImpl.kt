package com.equipo.sanmarkfood.comensal.data.repository

import com.equipo.sanmarkfood.comensal.data.firebase.ReservasDataSource
import com.equipo.sanmarkfood.comensal.data.firebase.aErrorReservas
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.comensal.domain.repository.ReservasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

class ReservasRepositoryImpl @Inject constructor(
    private val dataSource: ReservasDataSource,
) : ReservasRepository {

    override fun observarReservas(comensalUid: String): Flow<List<Reserva>> =
        dataSource.observarReservasDelComensal(comensalUid, RESERVAS_RECIENTES).catch { throw it.aErrorReservas() }

    override fun observarReserva(reservaId: String): Flow<Reserva?> =
        dataSource.observarReserva(reservaId).catch { throw it.aErrorReservas() }

    override fun observarOcupacion(restauranteId: String, fecha: String): Flow<Map<String, Int>> =
        dataSource.observarOcupacion(restauranteId, fecha).catch { throw it.aErrorReservas() }
}

private const val RESERVAS_RECIENTES = 30L
