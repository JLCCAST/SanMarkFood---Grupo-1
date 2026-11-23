package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.firebase.AuthDataSource
import com.equipo.sanmarkfood.restaurante.data.firebase.DashboardDataSource
import com.equipo.sanmarkfood.restaurante.data.remote.FuncionesApi
import com.equipo.sanmarkfood.restaurante.data.remote.RangoDto
import com.equipo.sanmarkfood.restaurante.data.remote.llamarFuncion
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.CifrasVentas
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.PlatoPedido
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.repository.DashboardRepository
import kotlinx.serialization.json.Json
import java.util.Date
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val dashboardDataSource: DashboardDataSource,
    private val funcionesApi: FuncionesApi,
    private val json: Json,
) : DashboardRepository {

    override suspend fun ventasEntre(desde: Date, hasta: Date): CifrasVentas =
        dashboardDataSource.ventasEntre(uid(), desde, hasta)

    override suspend fun reservasConfirmadasEntre(desde: Date, hasta: Date): Int =
        dashboardDataSource.reservasConfirmadasEntre(uid(), desde, hasta)

    override suspend fun promedioDeResenasHasta(hasta: Date): Double? =
        dashboardDataSource.promedioDeResenasHasta(uid(), hasta)

    override suspend fun platosMasPedidos(desde: Date, hasta: Date): List<PlatoPedido> =
        llamarFuncion(json, ErrorRestaurante.SinConexion, { ErrorRestaurante.Desconocido }) {
            funcionesApi.rankingPlatos(RangoDto(desde = desde.time, hasta = hasta.time))
        }.platos.map { PlatoPedido(nombre = it.nombre, cantidad = it.cantidad) }

    private fun uid(): String = authDataSource.usuarioActual()?.uid ?: throw ErrorRestaurante.Desconocido
}
