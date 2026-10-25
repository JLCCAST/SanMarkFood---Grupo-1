package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.PedidosDeHoy
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface PedidosRepository {
    fun observarPedidosDesde(desde: Date): Flow<PedidosDeHoy>
}
