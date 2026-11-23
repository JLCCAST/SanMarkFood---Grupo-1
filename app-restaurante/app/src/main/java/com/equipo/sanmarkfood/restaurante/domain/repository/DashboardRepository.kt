package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.CifrasVentas
import java.util.Date

interface DashboardRepository {
    suspend fun ventasEntre(desde: Date, hasta: Date): CifrasVentas

    suspend fun reservasConfirmadasEntre(desde: Date, hasta: Date): Int

    suspend fun promedioDeResenasHasta(hasta: Date): Double?
}
