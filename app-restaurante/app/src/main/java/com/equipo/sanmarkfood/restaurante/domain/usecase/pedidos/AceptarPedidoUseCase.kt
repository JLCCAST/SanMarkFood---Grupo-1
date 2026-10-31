package com.equipo.sanmarkfood.restaurante.domain.usecase.pedidos

import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.ErrorPedidos
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.EstadoPedido
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.TIEMPOS_ESTIMADOS
import com.equipo.sanmarkfood.restaurante.domain.repository.PedidosRepository
import javax.inject.Inject

class AceptarPedidoUseCase @Inject constructor(
    private val pedidosRepository: PedidosRepository
) {
    suspend operator fun invoke(pedido: Pedido, minutos: Int) {
        if (minutos !in TIEMPOS_ESTIMADOS) throw ErrorPedidos.Desconocido
        if (pedido.estado != EstadoPedido.RECIBIDO) throw ErrorPedidos.YaCambio
        pedidosRepository.aceptar(pedido.id, minutos)
    }
}
