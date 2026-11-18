package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.comensal.domain.repository.AuthRepository
import com.equipo.sanmarkfood.comensal.domain.repository.PedidosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObservarPedidoEnCursoUseCase @Inject constructor(
    private val pedidosRepository: PedidosRepository,
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<Pedido?> {
        val uid = authRepository.currentUser?.uid ?: return flowOf(null)
        return pedidosRepository.observarPedidosRecientes(uid).map { pedidos -> pedidos.firstOrNull { it.enCurso } }
    }
}
