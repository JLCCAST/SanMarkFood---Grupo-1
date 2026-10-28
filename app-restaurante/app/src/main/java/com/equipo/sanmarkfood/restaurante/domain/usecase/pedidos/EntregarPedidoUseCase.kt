package com.equipo.sanmarkfood.restaurante.domain.usecase.pedidos

import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.ErrorPedidos
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.EstadoPedido
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.restaurante.domain.repository.PedidosRepository
import javax.inject.Inject

class EntregarPedidoUseCase @Inject constructor(
    private val pedidosRepository: PedidosRepository
) {
    suspend operator fun invoke(pedido: Pedido, codigo: String? = null) {
        if (pedido.estado != EstadoPedido.LISTO && pedido.estado != EstadoPedido.EN_CAMINO) throw ErrorPedidos.YaCambio
        pedidosRepository.entregar(pedido.id, codigo)
    }
}
