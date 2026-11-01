package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.firebase.MetricasDataSource
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.repository.MetricasRepository
import java.util.Date
import javax.inject.Inject

class MetricasRepositoryImpl @Inject constructor(
    private val metricasDataSource: MetricasDataSource
) : MetricasRepository {

    override suspend fun contarRestaurantes(estado: EstadoRestaurante): Int =
        metricasDataSource.contarRestaurantes(estado)

    override suspend fun contarReenviados(): Int = metricasDataSource.contarReenviados()

    override suspend fun contarResenasReportadas(): Int = metricasDataSource.contarResenasReportadas()

    override suspend fun contarPedidos(desde: Date, hasta: Date): Int = metricasDataSource.contarPedidos(desde, hasta)
}
