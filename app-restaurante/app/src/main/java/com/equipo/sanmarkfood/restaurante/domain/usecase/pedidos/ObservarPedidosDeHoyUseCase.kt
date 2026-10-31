package com.equipo.sanmarkfood.restaurante.domain.usecase.pedidos

import com.equipo.sanmarkfood.restaurante.domain.model.menu.inicioDeHoy
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.PedidosDeHoy
import com.equipo.sanmarkfood.restaurante.domain.repository.PedidosRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservarPedidosDeHoyUseCase @Inject constructor(
    private val pedidosRepository: PedidosRepository
) {
    operator fun invoke(): Flow<PedidosDeHoy> = pedidosRepository.observarPedidosDesde(inicioDeHoy())
}
