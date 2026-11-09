package com.equipo.sanmarkfood.restaurante.domain.usecase.pedidos

import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.ErrorPedidos
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.EstadoPedido
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.restaurante.domain.repository.PedidosRepository
import javax.inject.Inject

class AvanzarPedidoUseCase @Inject constructor(
    private val pedidosRepository: PedidosRepository
) {
    suspend operator fun invoke(pedido: Pedido) {
        if (pedido.estado != EstadoPedido.PREPARANDO) throw ErrorPedidos.YaCambio
        pedidosRepository.avanzar(pedido.id)
    }
}
