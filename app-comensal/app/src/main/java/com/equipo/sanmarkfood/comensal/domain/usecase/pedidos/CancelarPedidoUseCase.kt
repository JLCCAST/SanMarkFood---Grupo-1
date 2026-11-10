package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorCancelarPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.EstadoPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.comensal.domain.repository.PedidosRepository
import javax.inject.Inject

class CancelarPedidoUseCase @Inject constructor(
    private val pedidosRepository: PedidosRepository,
) {
    suspend operator fun invoke(pedido: Pedido) {
        if (pedido.estado != EstadoPedido.RECIBIDO) throw ErrorCancelarPedido.YaAceptado
        pedidosRepository.cancelarPedido(pedido.id)
    }
}
