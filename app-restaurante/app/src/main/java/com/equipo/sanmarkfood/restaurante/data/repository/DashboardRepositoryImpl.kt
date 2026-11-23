package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.firebase.AuthDataSource
import com.equipo.sanmarkfood.restaurante.data.firebase.DashboardDataSource
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.CifrasVentas
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.repository.DashboardRepository
import java.util.Date
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val dashboardDataSource: DashboardDataSource,
) : DashboardRepository {

    override suspend fun ventasEntre(desde: Date, hasta: Date): CifrasVentas =
        dashboardDataSource.ventasEntre(uid(), desde, hasta)

    override suspend fun reservasConfirmadasEntre(desde: Date, hasta: Date): Int =
        dashboardDataSource.reservasConfirmadasEntre(uid(), desde, hasta)

    override suspend fun promedioDeResenasHasta(hasta: Date): Double? =
        dashboardDataSource.promedioDeResenasHasta(uid(), hasta)

    private fun uid(): String = authDataSource.usuarioActual()?.uid ?: throw ErrorRestaurante.Desconocido
}
