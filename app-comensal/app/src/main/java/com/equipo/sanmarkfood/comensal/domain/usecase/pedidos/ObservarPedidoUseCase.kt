package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.comensal.domain.repository.PedidosRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservarPedidoUseCase @Inject constructor(
    private val pedidosRepository: PedidosRepository,
) {
    operator fun invoke(pedidoId: String): Flow<Pedido?> = pedidosRepository.observarPedido(pedidoId)
}
