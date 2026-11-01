package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.admin.LocalAprobado
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import java.util.Date

interface MetricasRepository {
    suspend fun contarRestaurantes(estado: EstadoRestaurante): Int

    suspend fun contarReenviados(): Int

    suspend fun contarResenasReportadas(): Int

    suspend fun contarPedidos(desde: Date, hasta: Date): Int

    suspend fun listarAprobados(): List<LocalAprobado>

    suspend fun contarPedidosDeLocal(uid: String, desde: Date, hasta: Date): Int
}
