package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.firebase.AuthDataSource
import com.equipo.sanmarkfood.restaurante.data.firebase.ReservasDataSource
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.ReservasDeLaSemana
import com.equipo.sanmarkfood.restaurante.domain.repository.ReservasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ReservasRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val reservasDataSource: ReservasDataSource,
) : ReservasRepository {

    override fun observarReservasEntre(desde: String, hasta: String): Flow<ReservasDeLaSemana> = flow {
        val uid = authDataSource.usuarioActual()?.uid ?: throw ErrorReservas.Desconocido
        emitAll(reservasDataSource.observarEntre(uid, desde, hasta))
    }
}
