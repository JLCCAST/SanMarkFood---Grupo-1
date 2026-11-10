package com.equipo.sanmarkfood.comensal.domain.repository

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.DireccionEntrega
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MetodoPago
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Modalidad
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.PedidoCreado
import kotlinx.coroutines.flow.Flow

interface PedidosRepository {
    suspend fun crearPedido(
        carrito: Carrito,
        modalidad: Modalidad,
        metodoPago: MetodoPago,
        direccion: DireccionEntrega?,
    ): PedidoCreado

    fun observarPedido(pedidoId: String): Flow<Pedido?>

    suspend fun cancelarPedido(pedidoId: String)
}
