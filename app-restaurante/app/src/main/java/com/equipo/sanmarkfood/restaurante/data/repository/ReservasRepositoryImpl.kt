package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.firebase.AuthDataSource
import com.equipo.sanmarkfood.restaurante.data.firebase.ReservasDataSource
import com.equipo.sanmarkfood.restaurante.data.remote.AccionReservaDto
import com.equipo.sanmarkfood.restaurante.data.remote.FuncionesApi
import com.equipo.sanmarkfood.restaurante.data.remote.llamarFuncion
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.MotivoRechazoReserva
import com.equipo.sanmarkfood.restaurante.domain.model.reservas.ReservasDeLaSemana
import com.equipo.sanmarkfood.restaurante.domain.repository.ReservasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ReservasRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val reservasDataSource: ReservasDataSource,
    private val funcionesApi: FuncionesApi,
    private val json: Json,
) : ReservasRepository {

    override fun observarReservasEntre(desde: String, hasta: String): Flow<ReservasDeLaSemana> = flow {
        val uid = authDataSource.usuarioActual()?.uid ?: throw ErrorReservas.Desconocido
        emitAll(reservasDataSource.observarEntre(uid, desde, hasta))
    }

    override suspend fun confirmar(reservaId: String) {
        llamarReservas(AccionReservaDto(reservaId = reservaId, accion = "confirmar"))
    }

    override suspend fun rechazar(reservaId: String, motivo: MotivoRechazoReserva, detalle: String?) {
        llamarReservas(
            AccionReservaDto(reservaId = reservaId, accion = "rechazar", motivo = motivo.name.lowercase(), detalle = detalle)
        )
    }

    private suspend fun llamarReservas(accion: AccionReservaDto) {
        llamarFuncion(json, ErrorReservas.SinConexion, ::errorDeReserva) { funcionesApi.reservas(accion) }
    }

    private fun errorDeReserva(codigo: String?): Exception = when (codigo) {
        "estado_invalido" -> ErrorReservas.YaCambio
        else -> ErrorReservas.Desconocido
    }
}
