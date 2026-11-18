package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.MotivoRechazo
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.PedidosDeHoy
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface PedidosRepository {
    fun observarPedidosDesde(desde: Date): Flow<PedidosDeHoy>

    suspend fun aceptar(pedidoId: String, minutos: Int)

    suspend fun avanzar(pedidoId: String)

    suspend fun entregar(pedidoId: String, codigo: String?)

    suspend fun rechazar(pedidoId: String, motivo: MotivoRechazo, detalle: String?, platoAgotado: String?)
}
