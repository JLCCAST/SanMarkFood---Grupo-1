package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.repository.CarritoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservarCarritoUseCase @Inject constructor(
    private val carritoRepository: CarritoRepository
) {
    operator fun invoke(): Flow<Carrito?> = carritoRepository.observarCarrito()
}